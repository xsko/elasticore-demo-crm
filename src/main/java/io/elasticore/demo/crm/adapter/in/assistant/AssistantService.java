package io.elasticore.demo.crm.adapter.in.assistant;

import io.elasticore.demo.crm.infrastructure.semantic.MetricService;
import io.elasticore.demo.crm.infrastructure.semantic.SemanticModel;
import io.elasticore.springboot3.security.CurrentUser;
import io.elasticore.springboot3.security.NotSignedInException;
import io.elasticore.springboot3.security.SessionCurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The CRM's AI assistant. What it knows of the business comes from the DSL by way of the
 * Apache Ossie semantic model: each entity's description and synonyms (with the labels in
 * every language), its fields and the meaning of their codes, its lifecycle and the metrics.
 * What it can do are the tools made from the same model, run as the member who asks.
 *
 * <p>A customer gets the counsellor: help articles, the catalog and their own inquiries, and
 * it can open an inquiry. Staff get the sales assistant over the records they may see.
 */
@Service
public class AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);
    private static final int KEEP_MESSAGES = 16;
    private static final int KEEP_CONVERSATIONS = 500;

    public record Answer(String answer, List<Map<String, Object>> sources, String conversationId) {}

    private record Conversation(Object owner, List<Message> messages, Instant used) {}

    private final ObjectProvider<ChatModel> models;
    private final SemanticModel model;
    private final RecordAccess records;
    private final MetricService metrics;
    private final ObjectMapper json;
    private final List<String> customerEntities;
    private final List<String> customerCreatable;
    private final CurrentUser currentUser = new SessionCurrentUser();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    public AssistantService(ObjectProvider<ChatModel> models, SemanticModel model, RecordAccess records, MetricService metrics,
                            ObjectMapper json,
                            @Value("${crm.assistant.customer-entities:Ticket,KnowledgeArticle,Product}") String customerEntities,
                            @Value("${crm.assistant.customer-creatable:Ticket}") String customerCreatable) {
        this.models = models;
        this.model = model;
        this.records = records;
        this.metrics = metrics;
        this.json = json;
        this.customerEntities = split(customerEntities);
        this.customerCreatable = split(customerCreatable);
    }

    public boolean available() {
        return models.getIfAvailable() != null;
    }

    public Answer ask(String question, String conversationId, String language, String memberName) {
        if (!currentUser.signedIn()) throw new NotSignedInException("Sign in first");
        ChatModel chat = models.getIfAvailable();
        if (chat == null) throw new AssistantUnavailableException();

        boolean customer = currentUser.hasRole("CUSTOMER");
        List<String> readable = new ArrayList<>();
        for (SemanticModel.Dataset ds : model.datasets().values()) {
            if (!hasApi(ds)) continue;
            if (customer && !customerEntities.contains(ds.name())) continue;
            readable.add(ds.name());
        }
        List<String> creatable = customer ? customerCreatable : List.of();
        AssistantTools tools = new AssistantTools(records, metrics, model, json, readable, creatable);

        String id = conversationId != null && conversations.containsKey(conversationId) ? conversationId : UUID.randomUUID().toString();
        Conversation c = conversations.get(id);
        if (c != null && !String.valueOf(c.owner()).equals(String.valueOf(currentUser.id()))) {
            id = UUID.randomUUID().toString();                  // another member's conversation is never continued
            c = null;
        }
        List<Message> history = c == null ? new ArrayList<>() : new ArrayList<>(c.messages());

        long started = System.currentTimeMillis();
        String answer;
        try {
            answer = ChatClient.builder(chat).build().prompt()
                    .system(systemPrompt(customer, readable, creatable, language, memberName))
                    .messages(history)
                    .user(question)
                    .tools(tools.callbacks().toArray())
                    .call()
                    .content();
        } catch (RuntimeException e) {
            log.warn("assistant model call failed: {}", e.toString());
            throw new AssistantFailedException(e);
        }
        log.info("assistant answered in {} ms ({} sources, {})", System.currentTimeMillis() - started, tools.sources.size(),
                customer ? "customer" : "staff");

        history.add(new UserMessage(question));
        history.add(new AssistantMessage(answer == null ? "" : answer));
        while (history.size() > KEEP_MESSAGES) history.remove(0);
        conversations.put(id, new Conversation(currentUser.id(), history, Instant.now()));
        trim();

        List<Map<String, Object>> sources = new ArrayList<>();
        String said = answer == null ? "" : answer;
        for (AssistantTools.Source s : tools.sources) {
            if (sources.size() >= 8) break;
            if (s.title() == null || !said.contains(s.title())) continue;     // only what the answer used
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("entity", s.entity());
            m.put("id", s.id());
            m.put("title", s.title());
            if (s.id() != null) m.put("path", "/" + Character.toLowerCase(s.entity().charAt(0)) + s.entity().substring(1) + "/show/" + s.id());
            sources.add(m);
        }
        return new Answer(answer == null ? "" : answer.trim(), sources, id);
    }

    /** The model's knowledge of the business, written from the semantic model. */
    String systemPrompt(boolean customer, List<String> readable, List<String> creatable, String language, String memberName) {
        StringBuilder p = new StringBuilder();
        boolean english = "en".equalsIgnoreCase(language);
        if (customer) {
            p.append("You are the customer counsellor of a software company's CRM. You help a signed-in customer with their inquiries"
                    + " (tickets), with questions the help articles answer, and with the product catalog.\n");
            p.append("Answer a how-to or policy question from the help articles: search them first (several short keywords, one at a"
                    + " time). When the articles do not answer it, say so plainly and offer to open an inquiry; open one only when the"
                    + " customer agrees, with a clear subject, the question as the body and a fitting category.\n");
        } else {
            p.append("You are the sales assistant of a CRM. You answer a staff member's questions about the accounts, contacts, leads,"
                    + " opportunities, quotes, activities and inquiries they may see, and about the business metrics.\n");
            p.append("For a number the metrics define (pipeline, win rate, won amount ...), use get_metric rather than adding rows up.\n");
        }
        p.append("Use the tools for every fact about records; never invent a record, a name, an amount or a date. If a tool finds"
                + " nothing, say that nothing was found. Values shown masked (with *) stay masked. Mention the records you used by name.\n");
        p.append("Today is ").append(LocalDate.now()).append(". The member is ").append(memberName == null ? "signed in" : memberName).append(".\n");
        p.append(english ? "Answer in English.\n" : "한국어로 답하세요.\n");
        p.append("Keep answers short and concrete; use a short list for several records. Show a code by its meaning (the"
                + " meanings are listed with the fields), never the bare code. No emoji.\n\n");

        p.append("# The data you can read (entity: what it is | other names)\n");
        for (String name : readable) {
            SemanticModel.Dataset ds = model.dataset(name);
            p.append("## ").append(name).append(": ").append(ds.description());
            if (!ds.synonyms().isEmpty()) p.append(" | ").append(String.join(", ", ds.synonyms()));
            p.append('\n');
            for (SemanticModel.Field f : ds.fields()) {
                p.append("- ").append(AssistantTools.camel(f.name())).append(" (").append(f.datatype()).append(")");
                if (!f.description().isBlank()) p.append(": ").append(f.description());
                if (!f.synonyms().isEmpty()) p.append(" | ").append(String.join(", ", f.synonyms()));
                if (!f.context().isBlank()) p.append(" [").append(f.context()).append(']');
                p.append('\n');
            }
        }
        if (!customer && !model.metrics().isEmpty()) {
            p.append("\n# Metrics (get_metric)\n");
            for (SemanticModel.Metric m : model.metrics().values()) {
                p.append("- ").append(m.name()).append(" on ").append(m.dataset()).append(": ").append(m.description());
                if (!m.synonyms().isEmpty()) p.append(" | ").append(String.join(", ", m.synonyms()));
                p.append('\n');
            }
        }
        if (!creatable.isEmpty()) p.append("\nYou may create: ").append(String.join(", ", creatable)).append(".\n");
        return p.toString();
    }

    /** An entity with a generated API: one whose access the DSL states (the extension carries it). */
    private static boolean hasApi(SemanticModel.Dataset ds) {
        return !ds.access().isEmpty() && !"Member".equals(ds.name());
    }

    private void trim() {
        if (conversations.size() <= KEEP_CONVERSATIONS) return;
        conversations.entrySet().stream()
                .sorted((a, b) -> a.getValue().used().compareTo(b.getValue().used()))
                .limit(conversations.size() - KEEP_CONVERSATIONS)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(conversations::remove);
    }

    private static List<String> split(String s) {
        return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    /** The model did not answer (unreachable, refused the key ...): the endpoint answers 502. */
    public static class AssistantFailedException extends RuntimeException {
        public AssistantFailedException(Throwable cause) {
            super("The AI model did not answer", cause);
        }
    }

    /** No AI model is configured (CRM_AI_PROVIDER): the endpoint answers 503. */
    public static class AssistantUnavailableException extends RuntimeException {
        public AssistantUnavailableException() {
            super("The AI assistant is not configured on this server");
        }
    }
}

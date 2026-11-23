package io.elasticore.demo.crm.adapter.in.assistant;

import io.elasticore.demo.crm.infrastructure.semantic.MetricService;
import io.elasticore.demo.crm.infrastructure.semantic.SemanticModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The assistant's tools, made from the semantic model rather than written per entity:
 * search an entity, read one record, read a metric, and - for the entities allowed - create
 * a record. A new entity or metric in the DSL is a new choice in these tools' schemas with
 * the next build. Each call runs as the signed-in member (see RecordAccess), and every
 * record it reads is noted as a source of the answer.
 */
final class AssistantTools {

    record Source(String entity, Object id, String title) {}

    private final RecordAccess records;
    private final MetricService metrics;
    private final SemanticModel model;
    private final ObjectMapper json;
    private final List<String> readable;
    private final List<String> creatable;
    final List<Source> sources = new ArrayList<>();

    AssistantTools(RecordAccess records, MetricService metrics, SemanticModel model, ObjectMapper json,
                   List<String> readable, List<String> creatable) {
        this.records = records;
        this.metrics = metrics;
        this.model = model;
        this.json = json;
        this.readable = readable;
        this.creatable = creatable;
    }

    List<ToolCallback> callbacks() {
        List<ToolCallback> out = new ArrayList<>();
        out.add(tool("search_records",
                "Search the CRM's records of one entity. filters keys are the entity's field names (camelCase, as listed in the"
                        + " model); text fields match a part of the value; for a date or amount range use <field>From and <field>To;"
                        + " an enumeration takes its code; a reference takes the referenced key (accountId, ownerMemberId ...)."
                        + " sort is like \"amount-\" or \"name+\". Answers {total, rows}.",
                schema(Map.of(
                        "entity", Map.of("type", "string", "enum", readable),
                        "filters", Map.of("type", "object", "additionalProperties", true),
                        "sort", Map.of("type", "string"),
                        "limit", Map.of("type", "integer", "minimum", 1, "maximum", 50)), List.of("entity")),
                this::search));
        out.add(tool("get_record", "Read one record of an entity by its key.",
                schema(Map.of("entity", Map.of("type", "string", "enum", readable), "id", Map.of("type", "string")), List.of("entity", "id")),
                this::get));
        List<String> metricNames = new ArrayList<>(model.metrics().keySet());
        if (!metricNames.isEmpty()) {
            out.add(tool("get_metric",
                    "A business metric as the model defines it, for the records this member may see. by (optional) breaks it down by one"
                            + " field of the metric's entity (for example the stage).",
                    schema(Map.of("name", Map.of("type", "string", "enum", metricNames), "by", Map.of("type", "string")), List.of("name")),
                    this::metric));
        }
        if (!creatable.isEmpty()) {
            out.add(tool("create_record",
                    "Create a record - only when the member asked for it in this conversation (for example, to open an inquiry)."
                            + " values keys are the entity's field names; required fields must be given.",
                    schema(Map.of("entity", Map.of("type", "string", "enum", creatable),
                            "values", Map.of("type", "object", "additionalProperties", true)), List.of("entity", "values")),
                    this::create));
        }
        return out;
    }

    // ---- the tools ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Object search(Map<String, Object> in) {
        String entity = entity(in, readable);
        Map<String, Object> filters = in.get("filters") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        int limit = in.get("limit") instanceof Number n ? n.intValue() : 10;
        RecordAccess.Rows rows = records.search(entity, filters, (String) in.get("sort"), limit);
        for (Map<String, Object> r : rows.rows()) note(entity, r);
        return Map.of("total", rows.total(), "rows", rows.rows());
    }

    private Object get(Map<String, Object> in) {
        String entity = entity(in, readable);
        Map<String, Object> row = records.get(entity, in.get("id"));
        note(entity, row);
        return row;
    }

    private Object metric(Map<String, Object> in) {
        String name = String.valueOf(in.get("name"));
        Object by = in.get("by");
        return by == null || String.valueOf(by).isBlank() ? metrics.value(name) : metrics.by(name, String.valueOf(by));
    }

    @SuppressWarnings("unchecked")
    private Object create(Map<String, Object> in) {
        String entity = entity(in, creatable);
        Map<String, Object> values = in.get("values") instanceof Map<?, ?> m ? new LinkedHashMap<>((Map<String, Object>) m) : new LinkedHashMap<>();
        values.putIfAbsent("channel", "chatbot");
        Map<String, Object> row = records.create(entity, values);
        note(entity, row);
        return row;
    }

    // ---- helpers -------------------------------------------------------------------

    private static String entity(Map<String, Object> in, List<String> allowed) {
        String e = String.valueOf(in.get("entity"));
        if (!allowed.contains(e)) throw new IllegalArgumentException("entity must be one of " + allowed);
        return e;
    }

    private void note(String entity, Map<String, Object> row) {
        SemanticModel.Dataset ds = model.dataset(entity);
        if (ds == null || row == null) return;
        String key = ds.fields().isEmpty() ? null : camel(ds.fields().get(0).name());
        Object id = key == null ? null : row.get(key);
        String title = null;
        for (String n : List.of("name", "title", "subject", "company")) {
            if (row.get(n) != null) {
                title = String.valueOf(row.get(n));
                break;
            }
        }
        Source s = new Source(entity, id, title == null ? entity + " " + id : title);
        if (sources.stream().noneMatch(x -> x.entity().equals(s.entity()) && String.valueOf(x.id()).equals(String.valueOf(s.id())))) {
            sources.add(s);
        }
    }

    static String camel(String snake) {
        StringBuilder b = new StringBuilder();
        boolean up = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') up = true;
            else {
                b.append(up ? Character.toUpperCase(c) : c);
                up = false;
            }
        }
        return b.toString();
    }

    private String schema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("type", "object");
        s.put("properties", properties);
        s.put("required", required);
        return json.writeValueAsString(s);
    }

    private ToolCallback tool(String name, String description, String inputSchema, Function<Map<String, Object>, Object> fn) {
        ToolDefinition def = DefaultToolDefinition.builder().name(name).description(description).inputSchema(inputSchema).build();
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return def;
            }

            @Override
            @SuppressWarnings("unchecked")
            public String call(String input) {
                try {
                    Map<String, Object> args = input == null || input.isBlank() ? Map.of() : json.readValue(input, Map.class);
                    return json.writeValueAsString(fn.apply(args));
                } catch (RuntimeException e) {
                    // The model reads the refusal and can try again or say so.
                    return "{\"error\":" + json.writeValueAsString(String.valueOf(e.getMessage())) + "}";
                }
            }
        };
    }
}

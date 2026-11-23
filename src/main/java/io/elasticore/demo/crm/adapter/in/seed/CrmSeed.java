package io.elasticore.demo.crm.adapter.in.seed;

import io.elasticore.demo.crm.application.dto.AccountDTO;
import io.elasticore.demo.crm.application.dto.ActivityDTO;
import io.elasticore.demo.crm.application.dto.ContactDTO;
import io.elasticore.demo.crm.application.dto.KnowledgeArticleDTO;
import io.elasticore.demo.crm.application.dto.LeadDTO;
import io.elasticore.demo.crm.application.dto.MemberDTO;
import io.elasticore.demo.crm.application.dto.MemberSrchDTO;
import io.elasticore.demo.crm.application.dto.OpportunityDTO;
import io.elasticore.demo.crm.application.dto.ProductDTO;
import io.elasticore.demo.crm.application.dto.QuoteDTO;
import io.elasticore.demo.crm.application.dto.QuoteLineDTO;
import io.elasticore.demo.crm.application.dto.TicketDTO;
import io.elasticore.demo.crm.application.port.in.AccountUseCase;
import io.elasticore.demo.crm.application.port.in.ActivityUseCase;
import io.elasticore.demo.crm.application.port.in.ContactUseCase;
import io.elasticore.demo.crm.application.port.in.KnowledgeArticleUseCase;
import io.elasticore.demo.crm.application.port.in.LeadUseCase;
import io.elasticore.demo.crm.application.port.in.OpportunityUseCase;
import io.elasticore.demo.crm.application.port.in.ProductUseCase;
import io.elasticore.demo.crm.application.port.in.QuoteUseCase;
import io.elasticore.demo.crm.application.port.in.TicketUseCase;
import io.elasticore.demo.crm.application.MemberService;
import io.elasticore.demo.crm.domain.model.AccountTier;
import io.elasticore.demo.crm.domain.model.ActivityType;
import io.elasticore.demo.crm.domain.model.Industry;
import io.elasticore.demo.crm.domain.model.LeadSource;
import io.elasticore.demo.crm.domain.model.MemberRole;
import io.elasticore.demo.crm.domain.model.ProductCategory;
import io.elasticore.demo.crm.domain.model.TicketCategory;
import io.elasticore.demo.crm.domain.model.TicketPriority;
import io.elasticore.springboot3.domain.Money;
import io.elasticore.springboot3.domain.Quantity;
import io.elasticore.springboot3.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The data a fresh demo starts with: a sales team, a catalog, customer companies with
 * their people, leads, deals at every stage, quotes, activities and inquiries. Every row
 * goes through the generated services - prices are snapshotted, totals computed,
 * phone numbers and e-mails encrypted - exactly as a request would write it.
 *
 * <p>All companies and people are made up. It runs once, on a database without members, in the
 * language {@code crm.seed.language} names (en, the default, or ko - see {@link SeedText}).
 */
@Component
class CrmSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CrmSeed.class);

    /** The demo sign-ins; the same passwords are listed in README.md. */
    static final String DEMO_PASSWORD = "demo1234";

    private final MemberService members;
    private final ProductUseCase products;
    private final AccountUseCase accounts;
    private final ContactUseCase contacts;
    private final LeadUseCase leads;
    private final OpportunityUseCase opportunities;
    private final QuoteUseCase quotes;
    private final ActivityUseCase activities;
    private final TicketUseCase tickets;
    private final KnowledgeArticleUseCase articles;
    private final TransactionTemplate tx;
    private final boolean enabled;
    private final boolean demoAccounts;
    private final String adminLogin;
    private final String adminPassword;
    private final SeedText text;

    private final Random random = new Random(20261121L);

    CrmSeed(MemberService members, ProductUseCase products, AccountUseCase accounts, ContactUseCase contacts,
            LeadUseCase leads, OpportunityUseCase opportunities, QuoteUseCase quotes, ActivityUseCase activities,
            TicketUseCase tickets, KnowledgeArticleUseCase articles, TransactionTemplate tx,
            @Value("${crm.seed.enabled:true}") boolean enabled,
            @Value("${crm.seed.demo-accounts:true}") boolean demoAccounts,
            @Value("${crm.admin.login:admin}") String adminLogin,
            @Value("${crm.admin.password:}") String adminPassword,
            @Value("${crm.seed.language:en}") String language) {
        this.members = members;
        this.products = products;
        this.accounts = accounts;
        this.contacts = contacts;
        this.leads = leads;
        this.opportunities = opportunities;
        this.quotes = quotes;
        this.activities = activities;
        this.tickets = tickets;
        this.articles = articles;
        this.tx = tx;
        this.enabled = enabled;
        this.demoAccounts = demoAccounts;
        this.adminLogin = adminLogin;
        this.adminPassword = adminPassword;
        this.text = SeedText.of(language);
    }

    @Override
    public void run(ApplicationArguments args) {
        CurrentUser.runAsSystem(() -> {
            if (members.countBySearch(new MemberSrchDTO()) > 0) return;
            if (!demoAccounts) {
                if (adminPassword == null || adminPassword.isBlank()) {
                    throw new IllegalStateException("No member yet and no crm.admin.password (CRM_ADMIN_PASSWORD) to create the administrator with");
                }
                member(adminLogin, adminPassword, text.adminName, MemberRole.ADMIN, null);
                return;
            }
            long started = System.currentTimeMillis();
            tx.executeWithoutResult(s -> seed());
            log.info("sample data ({}) written in {} ms", text.language, System.currentTimeMillis() - started);
        });
    }

    // ---- the data ----------------------------------------------------------------

    /** The industry of each company suffix (Precision, Logistics, ...), in SeedText's order. */
    private static final Industry[] SUFFIX_INDUSTRY = {Industry.MANUFACTURING, Industry.LOGISTICS, Industry.HEALTHCARE,
            Industry.SOFTWARE, Industry.MANUFACTURING, Industry.MANUFACTURING, Industry.FINANCE, Industry.RETAIL,
            Industry.HEALTHCARE, Industry.SOFTWARE, Industry.SOFTWARE, Industry.MANUFACTURING, Industry.RETAIL,
            Industry.MANUFACTURING};

    private void seed() {
        List<Long> reps = new ArrayList<>();
        member("admin", DEMO_PASSWORD, text.adminName, MemberRole.ADMIN, text.staffCompany);
        for (String[] r : text.reps) {
            MemberDTO m = member(r[0], DEMO_PASSWORD, r[1], MemberRole.REP, text.staffCompany);
            reps.add(m.getMemberId());
        }
        List<Long> customers = new ArrayList<>();
        for (String[] c : text.customers) customers.add(member(c[0], DEMO_PASSWORD, c[1], MemberRole.CUSTOMER, c[2]).getMemberId());

        List<ProductDTO> catalog = products();
        knowledge();

        List<AccountDTO> accountList = new ArrayList<>();
        java.util.Set<String> used = new java.util.HashSet<>();
        for (int i = 0; i < 140; i++) {
            int p = random.nextInt(text.companyPrefix.length);
            int s = random.nextInt(text.companySuffix.length);
            String name = text.company(p, s);
            if (!used.add(name)) continue;
            AccountDTO a = new AccountDTO();
            a.setName(name);
            a.setIndustry(SUFFIX_INDUSTRY[s]);
            a.setTier(pick(AccountTier.STRATEGIC, AccountTier.KEY, AccountTier.KEY, AccountTier.STANDARD, AccountTier.STANDARD, AccountTier.STANDARD));
            a.setCity(text.cities[random.nextInt(text.cities.length)]);
            a.setPhone(text.office(300 + random.nextInt(600), random.nextInt(10000)));
            a.setWebsite("https://www.example-" + (accountList.size() + 1) + text.websiteSuffix);
            a.setAnnualRevenue(money((5 + random.nextInt(2000)) * 100_000_000L));
            a.setEmployees(20 + random.nextInt(3000));
            a.setOwnerMemberId(reps.get(random.nextInt(reps.size())));
            accountList.add(accounts.save(a));
        }

        List<ContactDTO> contactList = new ArrayList<>();
        for (AccountDTO a : accountList) {
            int n = 1 + random.nextInt(3);
            for (int i = 0; i < n; i++) {
                ContactDTO c = new ContactDTO();
                c.setName(personName());
                c.setJobTitle(text.jobTitles[random.nextInt(text.jobTitles.length)]);
                c.setEmail("contact" + (contactList.size() + 1) + "@example.com");
                c.setPhone(text.mobile(random.nextInt(10000), random.nextInt(10000)));
                c.setAccountId(a.getAccountId());
                c.setOwnerMemberId(a.getOwnerMemberId());
                contactList.add(contacts.save(c));
            }
        }

        for (int i = 0; i < 160; i++) {
            LeadDTO l = new LeadDTO();
            l.setCompany(text.company(random.nextInt(text.companyPrefix.length), random.nextInt(text.companySuffix.length)) + " " + pick(text.leadSites));
            l.setContactName(personName());
            l.setEmail("lead" + (i + 1) + "@example.com");
            l.setPhone(text.mobile(random.nextInt(10000), random.nextInt(10000)));
            l.setSource(pick(LeadSource.values()));
            l.setIndustry(pick(Industry.values()));
            l.setEstimatedValue(money((5 + random.nextInt(300)) * 1_000_000L));
            l.setOwnerMemberId(reps.get(random.nextInt(reps.size())));
            LeadDTO saved = leads.save(l);
            int fate = random.nextInt(10);
            if (fate >= 4) leads.qualify(saved.getLeadId());
            if (fate == 9) leads.convert(saved.getLeadId());
            else if (fate == 3 || fate == 8) leads.disqualify(saved.getLeadId());
        }

        List<OpportunityDTO> deals = new ArrayList<>();
        String[] deal = text.dealKinds;
        for (int i = 0; i < 300; i++) {
            AccountDTO a = accountList.get(random.nextInt(accountList.size()));
            OpportunityDTO o = new OpportunityDTO();
            o.setName(a.getName() + " " + deal[random.nextInt(deal.length)]);
            o.setAccountId(a.getAccountId());
            o.setAmount(money((10 + random.nextInt(900)) * 1_000_000L));
            o.setProbability(10 + random.nextInt(9) * 10);
            o.setExpectedCloseDate(LocalDate.now().plusDays(random.nextInt(240) - 90));
            o.setOwnerMemberId(a.getOwnerMemberId());
            OpportunityDTO saved = opportunities.save(o);
            Long id = saved.getOpportunityId();
            int stage = random.nextInt(12);
            if (stage >= 3) opportunities.propose(id);
            if (stage >= 6) opportunities.negotiate(id);
            if (stage >= 8 && stage <= 10) saved = opportunities.win(id);
            else if (stage == 11 || stage == 2) saved = opportunities.lose(id);
            deals.add(saved);
        }

        for (int i = 0; i < 70; i++) {
            OpportunityDTO o = deals.get(random.nextInt(deals.size()));
            QuoteDTO q = new QuoteDTO();
            q.setTitle(String.format(text.quoteTitle, o.getName(), 1 + random.nextInt(3)));
            q.setOpportunityId(o.getOpportunityId());
            q.setValidUntil(LocalDate.now().plusDays(15 + random.nextInt(45)));
            q.setOwnerMemberId(o.getOwnerMemberId());
            List<QuoteLineDTO> lines = new ArrayList<>();
            int n = 1 + random.nextInt(4);
            for (int k = 0; k < n; k++) {
                QuoteLineDTO line = new QuoteLineDTO();
                line.setProductId(catalog.get(random.nextInt(catalog.size())).getProductId());
                line.setQuantity(Quantity.of(1 + random.nextInt(20)));
                lines.add(line);
            }
            q.setLines(lines);
            QuoteDTO saved = quotes.save(q);
            int fate = random.nextInt(6);
            if (fate >= 2) quotes.send(saved.getQuoteId());
            if (fate == 4) quotes.accept(saved.getQuoteId());
            if (fate == 5) quotes.reject(saved.getQuoteId());
        }

        String[][] work = text.activities;
        for (int i = 0; i < 700; i++) {
            OpportunityDTO o = deals.get(random.nextInt(deals.size()));
            String[] w = work[random.nextInt(work.length)];
            ActivityDTO a = new ActivityDTO();
            a.setType(ActivityType.valueOf(w[1]));
            a.setSubject(w[0]);
            LocalDateTime due = LocalDateTime.now().withSecond(0).withNano(0).plusHours(random.nextInt(24 * 50) - 24 * 25);
            a.setDueAt(due.withMinute(random.nextBoolean() ? 0 : 30));
            a.setDone(due.isBefore(LocalDateTime.now()) && random.nextInt(5) > 0);
            a.setAccountId(o.getAccountId());
            a.setOpportunityId(o.getOpportunityId());
            a.setOwnerMemberId(o.getOwnerMemberId());
            activities.save(a);
        }

        String[][] asks = text.tickets;
        for (int i = 0; i < 48; i++) {
            String[] ask = asks[random.nextInt(asks.length)];
            Long customer = customers.get(random.nextInt(customers.size()));
            TicketDTO t = new TicketDTO();
            t.setSubject(ask[0]);
            t.setBody(ask[2]);
            t.setCategory(TicketCategory.valueOf(ask[1]));
            t.setPriority(pick(TicketPriority.LOW, TicketPriority.NORMAL, TicketPriority.NORMAL, TicketPriority.HIGH, TicketPriority.URGENT));
            t.setChannel(pick("web", "web", "chatbot", "phone"));
            t.setCustomerMemberId(customer);
            TicketDTO saved = tickets.save(t);
            int fate = random.nextInt(4);
            if (fate >= 1) {
                saved.setReply(text.ticketReply);
                tickets.update(saved);
                tickets.answer(saved.getTicketId());
            }
            if (fate == 3) tickets.close(saved.getTicketId());
        }
    }

    private List<ProductDTO> products() {
        List<ProductDTO> out = new ArrayList<>();
        for (String[] it : text.products) {
            ProductDTO p = new ProductDTO();
            p.setCode(it[0]);
            p.setName(it[1]);
            p.setCategory(ProductCategory.valueOf(it[2]));
            p.setPrice(money(Long.parseLong(it[3])));
            p.setActive(true);
            p.setDescription(String.format(text.productDescription, it[1]));
            out.add(products.save(p));
        }
        return out;
    }

    private void knowledge() {
        String[][] docs = text.articles;
        for (String[] d : docs) {
            KnowledgeArticleDTO a = new KnowledgeArticleDTO();
            a.setTitle(d[0]);
            a.setCategory(TicketCategory.valueOf(d[1]));
            a.setKeywords(d[2]);
            a.setBody(d[3]);
            a.setPublished(true);
            articles.save(a);
        }
    }

    private MemberDTO member(String login, String password, String name, MemberRole role, String company) {
        MemberDTO m = new MemberDTO();
        m.setLogin(login);
        m.setPassword(password);
        m.setName(name);
        m.setEmail(login + "@example.com");
        m.setPhone(text.mobile(random.nextInt(10000), random.nextInt(10000)));
        m.setCompany(company);
        m.setRole(role);
        return members.save(m);
    }

    private String personName() {
        int family = random.nextInt(text.familyNames.length);
        return text.person(family, random.nextInt(text.givenNames.length));
    }

    /** An amount written in won: as it is in Korean, in dollars (a thousandth) in English. */
    private Money money(long won) {
        return Money.of(BigDecimal.valueOf(won / text.moneyDivisor));
    }

    @SafeVarargs
    private <T> T pick(T... values) {
        return values[random.nextInt(values.length)];
    }
}

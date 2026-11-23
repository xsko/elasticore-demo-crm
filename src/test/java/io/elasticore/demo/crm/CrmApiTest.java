package io.elasticore.demo.crm;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * The CRM over HTTP, as a browser calls it (session cookie + X-XSRF-TOKEN), on the sample
 * data: the generated export of every matching row, who may do what (roles in @access),
 * masked values on an update, refusals in the answer's language, and the metrics.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmApiTest {

    private static final String TOKEN = "test-token";
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private MockHttpSession rep;
    private MockHttpSession admin;
    private MockHttpSession customer;

    @BeforeEach
    void signIn() throws Exception {
        rep = signIn("minjun");
        admin = signIn("admin");
        customer = signIn("customer");
    }

    // ---- the export ElastiCORE generates (@expose(methods="...,export")) --------------

    @Test
    void theExportHasEveryMatchingRowTitledInTheRequestsLanguage() throws Exception {
        long total = search(rep, "opportunity", "{\"pageSize\":1}").get("totalElements").asLong();
        String repName = body(mvc.perform(get("/api/crm/member/me").session(rep)).andReturn().getResponse()).get("name").asText();

        MockHttpServletResponse ko = export(rep, "opportunity", "{\"columns\":[\"name\",\"amount\",\"stage\",\"ownerName\"]}", "ko");
        assertThat(ko.getStatus()).isEqualTo(200);
        try (XSSFWorkbook wb = workbook(ko)) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo((int) total);
            assertThat(cells(sheet.getRow(0))).containsExactly("영업 기회명", "예상 금액", "단계", "담당자명");
            // A rep's file holds the rep's deals only, amounts as numbers, stages as their codes.
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                assertThat(row.getCell(3).getStringCellValue()).isEqualTo(repName);
                if (row.getCell(1) != null) assertThat(row.getCell(1).getNumericCellValue()).isGreaterThanOrEqualTo(0);
                assertThat(row.getCell(2).getStringCellValue()).isIn("PRS", "PRP", "NGT", "WON", "LST");
            }
        }

        MockHttpServletResponse en = export(rep, "opportunity", "{\"columns\":[\"name\",\"stage\"]}", "en");
        try (XSSFWorkbook wb = workbook(en)) {
            assertThat(cells(wb.getSheetAt(0).getRow(0))).containsExactly("Opportunity name", "Stage");
        }

        // "labels": "*" writes every enumeration by its label in the request's language (ElastiCORE #289).
        MockHttpServletResponse koLabels = export(rep, "opportunity", "{\"columns\":[\"name\",\"stage\"],\"labels\":\"*\"}", "ko");
        try (XSSFWorkbook wb = workbook(koLabels)) {
            assertThat(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue()).isIn("발굴", "제안", "협상", "수주", "실주");
        }
        MockHttpServletResponse enLabels = export(rep, "opportunity", "{\"columns\":[\"name\",\"stage\"],\"labels\":\"*\"}", "en");
        try (XSSFWorkbook wb = workbook(enLabels)) {
            assertThat(wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue()).isIn("Prospecting", "Proposal", "Negotiation", "Won", "Lost");
        }
    }

    @Test
    void theExportTakesTheScreensTitlesAndFollowsTheSearchAndItsOrder() throws Exception {
        long proposals = search(rep, "opportunity", "{\"stage\":\"PRP\",\"pageSize\":1}").get("totalElements").asLong();
        MockHttpServletResponse res = export(rep, "opportunity",
                "{\"search\":{\"stage\":\"PRP\"},\"sortCode\":\"amount-\",\"columns\":[\"name\",\"amount\"],\"headers\":[\"Deal\",\"Value\"]}", "ko");
        try (XSSFWorkbook wb = workbook(res)) {
            Sheet s = wb.getSheetAt(0);
            assertThat(cells(s.getRow(0))).containsExactly("Deal", "Value");
            assertThat(s.getLastRowNum()).isEqualTo((int) proposals);
            for (int r = 2; r <= s.getLastRowNum(); r++) {
                assertThat(s.getRow(r).getCell(1).getNumericCellValue()).isLessThanOrEqualTo(s.getRow(r - 1).getCell(1).getNumericCellValue());
            }
        }
    }

    @Test
    void theExportKeepsMaskedValuesMaskedAndAFormulaAsText() throws Exception {
        MockHttpServletResponse contacts = export(rep, "contact", "{\"columns\":[\"name\",\"phone\",\"email\"]}", "ko");
        try (XSSFWorkbook wb = workbook(contacts)) {
            Row row = wb.getSheetAt(0).getRow(1);
            assertThat(row.getCell(1).getStringCellValue()).contains("*");
            assertThat(row.getCell(2).getStringCellValue()).contains("*");
        }

        mvc.perform(withToken(post("/api/crm/account"), rep).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"=HYPERLINK(\\\"http://x\\\")\"}")).andReturn();
        // In a workbook a text is never a formula; in a CSV file it gets a ' before it.
        MockHttpServletResponse res = export(rep, "account", "{\"search\":{\"name\":\"HYPERLINK\"},\"columns\":[\"name\"]}", "ko");
        try (XSSFWorkbook wb = workbook(res)) {
            org.apache.poi.ss.usermodel.Cell cell = wb.getSheetAt(0).getRow(1).getCell(0);
            assertThat(cell.getCellType()).isEqualTo(org.apache.poi.ss.usermodel.CellType.STRING);
            assertThat(cell.getStringCellValue()).isEqualTo("=HYPERLINK(\"http://x\")");
        }
        MockHttpServletResponse csv = mvc.perform(withToken(post("/api/crm/account/export"), rep)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"search\":{\"name\":\"HYPERLINK\"},\"columns\":[\"name\"],\"format\":\"csv\"}")).andReturn().getResponse();
        String text = csv.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(text).startsWith("﻿").contains("\"'=HYPERLINK(\"\"http://x\"\")\"");
    }

    @Test
    void aVisitorOrACustomerCannotExportTheSalesRecords() throws Exception {
        assertThat(export(new MockHttpSession(), "opportunity", "{}", "ko").getStatus()).isEqualTo(401);
        assertThat(export(customer, "opportunity", "{}", "ko").getStatus()).isEqualTo(403);
    }

    // ---- roles in @access: read=REP&owner, read=owner|REP, answer=REP ---------------------

    @Test
    void aCustomerCannotReadDealsAndARepReadsAndAnswersEveryTicket() throws Exception {
        assertThat(searchStatus(customer, "opportunity", "{}")).isEqualTo(403);

        long everyTicket = search(admin, "ticket", "{\"pageSize\":1}").get("totalElements").asLong();
        long customersOwn = search(customer, "ticket", "{\"pageSize\":1}").get("totalElements").asLong();
        assertThat(search(rep, "ticket", "{\"pageSize\":1}").get("totalElements").asLong()).isEqualTo(everyTicket);
        assertThat(customersOwn).isPositive().isLessThan(everyTicket);

        JsonNode mine = body(mvc.perform(withToken(post("/api/crm/ticket"), customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"Invoice copy\",\"body\":\"Please send the invoice again.\",\"category\":\"BIL\"}")).andReturn().getResponse());
        long id = mine.get("ticketId").asLong();
        assertThat(mvc.perform(withToken(post("/api/crm/ticket/" + id + "/answer"), customer)).andReturn().getResponse().getStatus()).isEqualTo(403);

        MockHttpServletResponse answered = mvc.perform(withToken(post("/api/crm/ticket/" + id + "/answer"), rep)).andReturn().getResponse();
        assertThat(answered.getStatus()).isEqualTo(200);
        assertThat(body(answered).get("status").asText()).isEqualTo("ANS");
    }

    // ---- a masked value sent back unchanged keeps the stored one ---------------------------

    @Test
    void aMaskedValueSentBackUnchangedKeepsTheStoredOne() throws Exception {
        long accountId = search(rep, "account", "{\"pageSize\":1}").get("content").get(0).get("accountId").asLong();
        JsonNode created = body(mvc.perform(withToken(post("/api/crm/contact"), rep).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Round Trip\",\"phone\":\"010-2345-6789\",\"email\":\"round.trip@example.com\",\"accountId\":" + accountId + "}"))
                .andReturn().getResponse());
        assertThat(created.get("phone").asText()).isEqualTo("010-****-6789");

        // The form sends back what it was shown: the masked text.
        int status = mvc.perform(withToken(put("/api/crm/contact"), rep).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(created))).andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(200);
        assertThat(search(rep, "contact", "{\"phone\":\"010-2345-6789\"}").get("totalElements").asLong()).isEqualTo(1);
        assertThat(search(rep, "contact", "{\"email\":\"round.trip@example.com\"}").get("totalElements").asLong()).isEqualTo(1);

        // A new value typed over it is written.
        String changed = json.writeValueAsString(created).replace("010-****-6789", "010-9876-5432");
        mvc.perform(withToken(put("/api/crm/contact"), rep).contentType(MediaType.APPLICATION_JSON).content(changed)).andReturn();
        assertThat(search(rep, "contact", "{\"phone\":\"010-9876-5432\"}").get("totalElements").asLong()).isEqualTo(1);
    }

    // ---- refusals name the field, the entity and the state by their labels -----------------

    @Test
    void aRefusalNamesTheStateByItsLabelInTheAnswersLanguage() throws Exception {
        long accountId = search(rep, "account", "{\"pageSize\":1}").get("content").get(0).get("accountId").asLong();
        JsonNode deal = body(mvc.perform(withToken(post("/api/crm/opportunity"), rep).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Too early\",\"accountId\":" + accountId + "}")).andReturn().getResponse());
        long id = deal.get("opportunityId").asLong();

        MockHttpServletResponse ko = mvc.perform(withToken(post("/api/crm/opportunity/" + id + "/win"), rep)
                .header("Accept-Language", "ko")).andReturn().getResponse();
        assertThat(ko.getStatus()).isBetween(400, 499);
        assertThat(body(ko).get("detail").asText()).startsWith("단계가 발굴일 때는 수주로 바꿀 수 없습니다").contains("제안").contains("협상");

        MockHttpServletResponse en = mvc.perform(withToken(post("/api/crm/opportunity/" + id + "/win"), rep)
                .header("Accept-Language", "en")).andReturn().getResponse();
        assertThat(body(en).get("detail").asText()).contains("Prospecting").contains("Stage");
    }

    // ---- metrics and the guards around them ------------------------------------------------

    @Test
    void aRepsMetricsCountTheirOwnRecordsTheAdministratorsEveryone() throws Exception {
        BigDecimal mine = metric(rep, "pipelineAmount");
        BigDecimal all = metric(admin, "pipelineAmount");
        assertThat(mine).isPositive();
        assertThat(all).isGreaterThan(mine);

        BigDecimal sum = BigDecimal.ZERO;
        for (String stage : List.of("PRS", "PRP", "NGT")) {
            JsonNode page = search(rep, "opportunity", "{\"stage\":\"" + stage + "\",\"pageSize\":500}");
            for (JsonNode o : page.get("content")) sum = sum.add(new BigDecimal(o.get("amount").asText()));
        }
        assertThat(mine).isEqualByComparingTo(sum);
    }

    @Test
    void aWriteWithoutTheXsrfTokenIsRefused() throws Exception {
        int status = mvc.perform(post("/api/crm/account").session(rep).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"No token\"}")).andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(403);
    }

    // ---- helpers -------------------------------------------------------------------

    private MockHttpSession signIn(String login) throws Exception {
        MockHttpSession session = new MockHttpSession();
        org.springframework.test.web.servlet.MvcResult result = mvc.perform(withToken(post("/api/crm/member/signin"), session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"" + login + "\",\"password\":\"demo1234\"}")).andReturn();
        assertThat(result.getResponse().getStatus()).as("sign-in of " + login).isEqualTo(200);
        // The sign-in starts a new session (the id changes): continue with the one it made.
        MockHttpSession now = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(now).isNotNull();
        int me = mvc.perform(get("/api/crm/member/me").session(now)).andReturn().getResponse().getStatus();
        assertThat(me).as("me after sign-in").isEqualTo(200);
        return now;
    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder b, MockHttpSession session) {
        return b.session(session).cookie(new Cookie("XSRF-TOKEN", TOKEN)).header("X-XSRF-TOKEN", TOKEN);
    }

    private JsonNode search(MockHttpSession session, String entity, String body) throws Exception {
        MockHttpServletResponse res = mvc.perform(withToken(post("/api/crm/" + entity + "/search"), session)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
        assertThat(res.getStatus()).as("search of " + entity + ": " + res.getContentAsString()).isEqualTo(200);
        return body(res);
    }

    private int searchStatus(MockHttpSession session, String entity, String body) throws Exception {
        return mvc.perform(withToken(post("/api/crm/" + entity + "/search"), session)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus();
    }

    private JsonNode body(MockHttpServletResponse res) throws Exception {
        return json.readTree(res.getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    private BigDecimal metric(MockHttpSession session, String name) throws Exception {
        String text = mvc.perform(get("/api/crm/metrics/" + name).session(session)).andReturn().getResponse().getContentAsString();
        return new BigDecimal(json.readTree(text).get("value").asText());
    }

    /** POST /api/crm/{entity}/export, asking for an .xlsx workbook. */
    private MockHttpServletResponse export(MockHttpSession session, String entity, String body, String lang) throws Exception {
        return mvc.perform(withToken(post("/api/crm/" + entity + "/export"), session)
                .header("Accept-Language", lang)
                .accept(XLSX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)).andReturn().getResponse();
    }

    private static XSSFWorkbook workbook(MockHttpServletResponse res) throws Exception {
        assertThat(res.getStatus()).as(res.getContentAsString()).isEqualTo(200);
        assertThat(res.getContentType()).startsWith(XLSX);
        return new XSSFWorkbook(new ByteArrayInputStream(res.getContentAsByteArray()));
    }

    private static List<String> cells(Row row) {
        List<String> out = new ArrayList<>();
        row.forEach(c -> out.add(c.getStringCellValue()));
        return out;
    }
}

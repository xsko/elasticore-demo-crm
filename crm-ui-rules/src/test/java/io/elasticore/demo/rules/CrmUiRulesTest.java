package io.elasticore.demo.rules;

import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.ModelCheck;
import io.elasticore.base.model.pub.rules.RuleCatalog;
import io.elasticore.base.model.pub.rules.SourceCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The CRM's screen-model rule, run on a small model written here - the way ElastiCORE's
 * rule guide tests rules (elasticore-rule-dev-guide.md, "Testing rules") - without a build.
 */
class CrmUiRulesTest {

    @AfterEach
    void clear() {
        ModelCheck.reset();
    }

    /** Like model.yml: Contact @root @expose(20) @ui(menu=sales, order=40, board) with a few fields. */
    private static EntitySpec contact() {
        return new EntitySpec("Contact")
                .domain("crm", "Crm")
                .pkg("entity", "io.elasticore.demo.crm.domain.model")
                .pkg("dto", "io.elasticore.demo.crm.application.dto")
                .meta("root", "")
                .meta("expose", "20")
                .meta("label", "연락처")
                .labelTranslations(Map.of("en", "Contact"))
                .metaProperty("ui", "menu", "sales")
                .metaProperty("ui", "order", "40")
                .metaFlags("ui", List.of("board"))
                .config("crm.languages", "en")
                .field("contactId", "long @id @label(en=Contact no.) -- 연락처 번호")
                .field("name", "string(50)! @search(like) @label(en=Name) -- 이름")
                .field("amount", "money(15.2) @label(en=Amount) -- 금액")
                .field("note", "text @label(en=Note) -- 메모");
    }

    private static String screenModel(EntitySpec e) {
        return RuleCatalog.standard().engineFor("ts-model")
                .register(new CrmUiRules())
                .templateSet(SourceCode.TemplateSet.TYPESCRIPT)
                .run(e, CrmUiRules.TARGET)
                .render();
    }

    @Test
    void theScreenModelCarriesKindsLabelsAndTheMenuPlace() {
        String ts = screenModel(contact());
        assertTrue(ts.contains("name: \"Contact\""), ts);
        assertTrue(ts.contains("path: \"/api/crm/contact\""), ts);
        assertTrue(ts.contains("pageSize: 20"), ts);
        assertTrue(ts.contains("labels: { en: \"Contact\" }"), ts);
        assertTrue(ts.contains("menu: \"sales\""), ts);
        assertTrue(ts.contains("order: 40"), ts);
        assertTrue(ts.contains("board: true"), ts);
        assertTrue(ts.contains("kind: \"money\""), ts);
        assertTrue(ts.contains("kind: \"text\""), ts);
        assertTrue(ts.contains("search: \"like\""), ts);
        assertTrue(ModelCheck.failures().isEmpty(), ModelCheck.failures().toString());
    }

    @Test
    void thePageSizeIsReadBesideTheMethodsToo() {
        // @expose(20, methods="read,search"): the size is a bare value beside a key=value pair
        EntitySpec e = contact().meta("expose", "").metaFlags("expose", List.of("50")).metaProperty("expose", "methods", "read,search");
        assertTrue(screenModel(e).contains("pageSize: 50"));
    }

    @Test
    void aFieldWithoutItsEnglishLabelStopsTheBuild() {
        EntitySpec e = contact().field("segment", "string(30) -- 고객 세그먼트");
        screenModel(e);
        assertTrue(ModelCheck.failures().stream().anyMatch(f -> f.contains("segment") && f.contains("no label in en")),
                ModelCheck.failures().toString());
    }

    @Test
    void theBlindIndexOfAnEncryptedFieldIsNoScreenField() {
        EntitySpec e = contact()
                .field("phone", "string(20) @encrypt(digits) @s(eq) @label(en=Phone) -- 전화번호")
                .field("phoneBidx", "string(22) @bidxof(phone)");
        String ts = screenModel(e);
        assertTrue(ts.contains("name: \"phone\""), ts);
        assertFalse(ts.contains("phoneBidx"), ts);
    }
}

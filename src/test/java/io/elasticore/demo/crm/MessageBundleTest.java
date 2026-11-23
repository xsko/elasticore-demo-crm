package io.elasticore.demo.crm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every message the API can answer with has its Korean wording, and every label of the model
 * has its English one. A field, a rule or a state added to the DSL adds keys to the generated
 * bundles; this test names the ones still to translate.
 */
class MessageBundleTest {

    @Test
    void everyMessageHasItsKoreanWording() throws IOException {
        assertCovered("i18n/crm-messages.properties", "i18n/crm-messages_ko.properties");
    }

    @Test
    void everyLabelHasItsEnglishWording() throws IOException {
        assertCovered("i18n/crm-labels.properties", "i18n/crm-labels_en.properties");
    }

    private static void assertCovered(String base, String translation) throws IOException {
        Properties from = load(base);
        Properties to = load(translation);
        Set<String> missing = new TreeSet<>(from.stringPropertyNames());
        missing.removeAll(to.stringPropertyNames());
        assertTrue(missing.isEmpty(), translation + " has no wording for " + missing);
    }

    private static Properties load(String path) throws IOException {
        Properties p = new Properties();
        try (InputStream in = MessageBundleTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IOException("no " + path);
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }
}

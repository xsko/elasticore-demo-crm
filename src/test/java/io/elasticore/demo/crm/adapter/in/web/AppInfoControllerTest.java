package io.elasticore.demo.crm.adapter.in.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The currency the screens show amounts in: the setting's, else the sample data's. */
class AppInfoControllerTest {

    @Test
    void englishDataIsInDollarsAndKoreanInWon() {
        assertThat(new AppInfoController("", "en").info()).containsEntry("currency", "USD").containsEntry("dataLanguage", "en");
        assertThat(new AppInfoController("", "ko").info()).containsEntry("currency", "KRW").containsEntry("dataLanguage", "ko");
        assertThat(new AppInfoController(null, null).info()).containsEntry("currency", "USD").containsEntry("dataLanguage", "en");
    }

    @Test
    void aCurrencySetOnItsOwnWins() {
        assertThat(new AppInfoController("eur", "ko").info()).containsEntry("currency", "EUR");
    }

    @Test
    void somethingThatIsNoCurrencyCodeIsIgnored() {
        assertThat(new AppInfoController("dollars", "en").info()).containsEntry("currency", "USD");
        assertThat(new AppInfoController("₩", "ko").info()).containsEntry("currency", "KRW");
    }
}

package io.elasticore.demo.crm.adapter.in.seed;

import io.elasticore.demo.crm.domain.model.ActivityType;
import io.elasticore.demo.crm.domain.model.ProductCategory;
import io.elasticore.demo.crm.domain.model.TicketCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * The sample data's words. The seeder draws the same random numbers in either language, so
 * both sets must have lists of the same length - else the English and the Korean data would
 * differ in shape, not just in words.
 */
class SeedTextTest {

    private final SeedText en = SeedText.ENGLISH;
    private final SeedText ko = SeedText.KOREAN;

    @Test
    void bothLanguagesHaveListsOfTheSameLength() {
        assertThat(en.reps.length).isEqualTo(ko.reps.length);
        assertThat(en.customers.length).isEqualTo(ko.customers.length);
        assertThat(en.companyPrefix.length).isEqualTo(ko.companyPrefix.length);
        assertThat(en.companySuffix.length).isEqualTo(ko.companySuffix.length);
        assertThat(en.cities.length).isEqualTo(ko.cities.length);
        assertThat(en.familyNames.length).isEqualTo(ko.familyNames.length);
        assertThat(en.givenNames.length).isEqualTo(ko.givenNames.length);
        assertThat(en.jobTitles.length).isEqualTo(ko.jobTitles.length);
        assertThat(en.leadSites.length).isEqualTo(ko.leadSites.length);
        assertThat(en.dealKinds.length).isEqualTo(ko.dealKinds.length);
        assertThat(en.activities.length).isEqualTo(ko.activities.length);
        assertThat(en.tickets.length).isEqualTo(ko.tickets.length);
        assertThat(en.products.length).isEqualTo(ko.products.length);
        assertThat(en.articles.length).isEqualTo(ko.articles.length);
    }

    @Test
    void theDemoLoginsAreTheSameInBothLanguages() {
        for (int i = 0; i < en.reps.length; i++) assertThat(en.reps[i][0]).isEqualTo(ko.reps[i][0]);
        for (int i = 0; i < en.customers.length; i++) assertThat(en.customers[i][0]).isEqualTo(ko.customers[i][0]);
    }

    @Test
    void everyCodeTheTextsNameIsAConstantOfItsEnumeration() {
        for (SeedText t : List.of(en, ko)) {
            for (String[] a : t.activities) assertThatCode(() -> ActivityType.valueOf(a[1])).doesNotThrowAnyException();
            for (String[] k : t.tickets) assertThatCode(() -> TicketCategory.valueOf(k[1])).doesNotThrowAnyException();
            for (String[] p : t.products) assertThatCode(() -> ProductCategory.valueOf(p[2])).doesNotThrowAnyException();
            for (String[] d : t.articles) assertThatCode(() -> TicketCategory.valueOf(d[1])).doesNotThrowAnyException();
        }
    }

    @Test
    void theSameProductsAndPricesInBothLanguages() {
        for (int i = 0; i < en.products.length; i++) {
            assertThat(en.products[i][0]).isEqualTo(ko.products[i][0]);        // code
            assertThat(en.products[i][3]).isEqualTo(ko.products[i][3]);        // price in won
        }
        assertThat(en.moneyDivisor).isEqualTo(1000);
        assertThat(ko.moneyDivisor).isEqualTo(1);
    }

    @Test
    void namesAreWrittenTheWayEachLanguageWritesThem() {
        assertThat(en.person(0, 0)).isEqualTo("Olivia Smith");
        assertThat(ko.person(0, 0)).isEqualTo("김도윤");
        assertThat(en.company(0, 0)).isEqualTo("Bright Precision");
        assertThat(ko.company(0, 0)).isEqualTo("다온정밀");
    }

    @Test
    void phoneNumbersHaveThreeGroupsSoTheMaskHidesTheMiddleOne() {
        assertThat(en.mobile(1234, 5678)).matches("\\d{3}-555-5678");
        assertThat(ko.mobile(1234, 5678)).isEqualTo("010-1234-5678");
        assertThat(en.office(512, 42)).isEqualTo("512-555-0042");
        assertThat(ko.office(512, 42)).isEqualTo("02-512-0042");
    }

    @Test
    void theLanguageIsReadLeniently() {
        assertThat(SeedText.of("ko")).isSameAs(ko);
        assertThat(SeedText.of(" KO-kr ")).isSameAs(ko);
        assertThat(SeedText.of("en")).isSameAs(en);
        assertThat(SeedText.of(null)).isSameAs(en);
        assertThat(SeedText.of("fr")).isSameAs(en);
    }
}

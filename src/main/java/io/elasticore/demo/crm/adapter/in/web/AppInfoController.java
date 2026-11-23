package io.elasticore.demo.crm.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * What the screens need to know about this installation before anyone signs in: the
 * currency amounts are kept in (crm.currency; by default the sample data's - dollars for
 * the English data, won for the Korean) and the language the sample data is written in.
 */
@RestController
@RequestMapping("/api/crm/app")
public class AppInfoController {

    private final String currency;
    private final String dataLanguage;

    public AppInfoController(@Value("${crm.currency:}") String currency,
                             @Value("${crm.seed.language:en}") String dataLanguage) {
        String language = dataLanguage == null || dataLanguage.isBlank() ? "en" : dataLanguage.trim().toLowerCase();
        this.dataLanguage = language.startsWith("ko") ? "ko" : "en";
        this.currency = currency != null && currency.matches("[A-Za-z]{3}")
                ? currency.toUpperCase()
                : ("ko".equals(this.dataLanguage) ? "KRW" : "USD");
    }

    @GetMapping
    public Map<String, String> info() {
        return Map.of("currency", currency, "dataLanguage", dataLanguage);
    }
}

package io.elasticore.demo.crm.adapter.in.web.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/** The two hand-written filters every answer passes through: the CSRF guard and the security headers. */
class SecurityFiltersTest {

    private final CsrfCookieFilter csrf = new CsrfCookieFilter();
    private final SecurityHeadersFilter headers = new SecurityHeadersFilter();

    // ---- CSRF: a write under /api/ must echo the XSRF-TOKEN cookie in the X-XSRF-TOKEN header ----

    @Test
    void aFirstVisitGetsATokenCookieScriptsCanRead() throws Exception {
        MockHttpServletResponse res = run(csrf, request("GET", "/api/crm/app", null, null));
        String cookie = res.getHeader("Set-Cookie");
        assertThat(cookie).startsWith("XSRF-TOKEN=").contains("SameSite=Lax").doesNotContain("HttpOnly");
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void aWriteWithoutTheHeaderIsRefused() throws Exception {
        MockHttpServletResponse res = run(csrf, request("POST", "/api/crm/account", "t0ken", null));
        assertThat(res.getStatus()).isEqualTo(403);
        assertThat(res.getContentAsString()).contains("\"code\":\"csrf\"");
    }

    @Test
    void aWriteWithAnotherTokenIsRefused() throws Exception {
        assertThat(run(csrf, request("PUT", "/api/crm/account", "t0ken", "other")).getStatus()).isEqualTo(403);
        assertThat(run(csrf, request("DELETE", "/api/crm/account/1", null, "t0ken")).getStatus()).isEqualTo(403);
    }

    @Test
    void aWriteThatEchoesTheCookieGoesThrough() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse res = new MockHttpServletResponse();
        csrf.doFilter(request("POST", "/api/crm/account", "t0ken", "t0ken"), res, chain);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    void aReadNeedsNoToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        csrf.doFilter(request("GET", "/api/crm/account/1", null, null), new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    // ---- security headers ---------------------------------------------------------------

    @Test
    void everyPageGetsTheContentSecurityPolicyAndNoFraming() throws Exception {
        MockHttpServletResponse res = run(headers, request("GET", "/", null, null));
        assertThat(res.getHeader("Content-Security-Policy"))
                .contains("script-src 'self'").contains("frame-ancestors 'none'").contains("object-src 'none'");
        assertThat(res.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(res.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(res.getHeader("Referrer-Policy")).isEqualTo("same-origin");
        assertThat(res.getHeader("Cache-Control")).isNull();
    }

    @Test
    void apiAnswersAreNotCached() throws Exception {
        assertThat(run(headers, request("GET", "/api/crm/account/1", null, null)).getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    void theApiDocumentationKeepsTheBrowserDefaults() throws Exception {
        MockHttpServletResponse res = run(headers, request("GET", "/swagger-ui/index.html", null, null));
        assertThat(res.getHeader("Content-Security-Policy")).isNull();
        assertThat(res.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    }

    // ---- helpers --------------------------------------------------------------------------

    private static MockHttpServletRequest request(String method, String uri, String cookie, String header) {
        MockHttpServletRequest r = new MockHttpServletRequest(method, uri);
        if (cookie != null) r.setCookies(new Cookie(CsrfCookieFilter.COOKIE, cookie));
        if (header != null) r.addHeader(CsrfCookieFilter.HEADER, header);
        return r;
    }

    private static MockHttpServletResponse run(jakarta.servlet.Filter filter, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(request, res, new MockFilterChain());
        return res;
    }
}

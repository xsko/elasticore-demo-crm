package io.elasticore.demo.crm.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Cross-site request forgery guard for the cookie session (double-submit token).
 *
 * <p>The generated sign-in keeps the member in a session cookie, which a browser also sends
 * with a request another site makes it send. Every response gives the browser a random
 * {@code XSRF-TOKEN} cookie that scripts of this origin can read; a request that changes
 * something ({@code POST}, {@code PUT}, {@code PATCH}, {@code DELETE} under {@code /api/})
 * must echo it in the {@code X-XSRF-TOKEN} header. Another origin can neither read the
 * cookie nor set the header, so its forged request is refused with 403.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CsrfCookieFilter extends OncePerRequestFilter {

    public static final String COOKIE = "XSRF-TOKEN";
    public static final String HEADER = "X-XSRF-TOKEN";
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = cookie(request);
        if (token == null) {
            token = newToken();
            ResponseCookie c = ResponseCookie.from(COOKIE, token)
                    .path("/").sameSite("Lax").secure(request.isSecure()).httpOnly(false).build();
            response.addHeader(HttpHeaders.SET_COOKIE, c.toString());
        }
        if (!SAFE.contains(request.getMethod()) && request.getRequestURI().startsWith("/api/")) {
            String sent = request.getHeader(HEADER);
            if (sent == null || !same(sent, cookie(request))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/problem+json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":403,\"code\":\"csrf\",\"title\":\"Forbidden\","
                        + "\"detail\":\"The request did not carry this site's X-XSRF-TOKEN.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static String cookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (COOKIE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) return c.getValue();
        }
        return null;
    }

    private static boolean same(String a, String b) {
        if (b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

package io.elasticore.demo.crm.application.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: after a number of wrong passwords for one login, or from one
 * address, sign-in is refused for a while - with the same answer as a wrong password, so the
 * lock tells nobody whether the login exists. A right password resets the login's count.
 */
@Component
public class SignInThrottle {

    private final int maxFailures;
    private final Duration lock;
    private final Clock clock;
    private final Map<String, Failures> byKey = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public SignInThrottle(@Value("${crm.security.max-failed-sign-ins:5}") int maxFailures,
                          @Value("${crm.security.lock-minutes:10}") int lockMinutes) {
        this(maxFailures, Duration.ofMinutes(lockMinutes), Clock.systemUTC());
    }

    SignInThrottle(int maxFailures, Duration lock, Clock clock) {
        this.maxFailures = maxFailures;
        this.lock = lock;
        this.clock = clock;
    }

    /** Whether this login, from where the request comes, may try now. */
    public boolean allowed(String login) {
        Instant now = clock.instant();
        return !locked(loginKey(login), now) && !locked(addressKey(), now);
    }

    public void failed(String login) {
        Instant now = clock.instant();
        bump(loginKey(login), now);
        // An address gets a wider allowance: several people may share it.
        bump(addressKey(), now);
    }

    public void succeeded(String login) {
        byKey.remove(loginKey(login));
    }

    private boolean locked(String key, Instant now) {
        if (key == null) return false;
        Failures f = byKey.get(key);
        if (f == null) return false;
        if (f.until != null && now.isBefore(f.until)) return true;
        if (f.until != null) byKey.remove(key);
        return false;
    }

    private void bump(String key, Instant now) {
        if (key == null) return;
        int limit = key.startsWith("ip:") ? maxFailures * 4 : maxFailures;
        byKey.compute(key, (k, f) -> {
            Failures next = f == null || (f.until != null && !now.isBefore(f.until)) ? new Failures() : f;
            next.count++;
            if (next.count >= limit) next.until = now.plus(lock);
            return next;
        });
    }

    private static String loginKey(String login) {
        return login == null ? null : "login:" + login.trim().toLowerCase(Locale.ROOT);
    }

    private static String addressKey() {
        RequestAttributes a = RequestContextHolder.getRequestAttributes();
        if (!(a instanceof ServletRequestAttributes s)) return null;
        HttpServletRequest r = s.getRequest();
        return "ip:" + r.getRemoteAddr();
    }

    private static final class Failures {
        int count;
        Instant until;
    }
}

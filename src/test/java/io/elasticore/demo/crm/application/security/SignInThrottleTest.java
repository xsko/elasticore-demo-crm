package io.elasticore.demo.crm.application.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/** Password guessing: per login, per address, for a while - and a right password starts over. */
class SignInThrottleTest {

    private final MovableClock clock = new MovableClock();
    private final SignInThrottle throttle = new SignInThrottle(5, Duration.ofMinutes(10), clock);

    @AfterEach
    void noRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void fiveWrongPasswordsLockTheLoginAndNoOtherOne() {
        for (int i = 0; i < 4; i++) throttle.failed("minjun");
        assertThat(throttle.allowed("minjun")).isTrue();
        throttle.failed("minjun");
        assertThat(throttle.allowed("minjun")).isFalse();
        assertThat(throttle.allowed("seoyeon")).isTrue();
    }

    @Test
    void theLockEndsAfterItsTime() {
        for (int i = 0; i < 5; i++) throttle.failed("minjun");
        clock.advance(Duration.ofMinutes(9));
        assertThat(throttle.allowed("minjun")).isFalse();
        clock.advance(Duration.ofMinutes(1));
        assertThat(throttle.allowed("minjun")).isTrue();
    }

    @Test
    void aRightPasswordStartsTheCountOver() {
        for (int i = 0; i < 4; i++) throttle.failed("minjun");
        throttle.succeeded("minjun");
        for (int i = 0; i < 4; i++) throttle.failed("minjun");
        assertThat(throttle.allowed("minjun")).isTrue();
    }

    @Test
    void theLoginIsTheSameWhateverItsCaseAndSpaces() {
        for (int i = 0; i < 5; i++) throttle.failed(i % 2 == 0 ? " MinJun " : "minjun");
        assertThat(throttle.allowed("MINJUN")).isFalse();
    }

    @Test
    void oneAddressGetsFourTimesTheAllowanceAcrossLogins() {
        from("203.0.113.7");
        for (int i = 0; i < 19; i++) throttle.failed("guess" + i);       // each login stays under 5
        assertThat(throttle.allowed("someone-else")).isTrue();
        throttle.failed("guess19");
        assertThat(throttle.allowed("someone-else")).isFalse();          // the address is locked

        from("198.51.100.1");
        assertThat(throttle.allowed("someone-else")).isTrue();           // another address is not
    }

    private static void from(String address) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(address);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    /** A clock the test moves. */
    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-22T09:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

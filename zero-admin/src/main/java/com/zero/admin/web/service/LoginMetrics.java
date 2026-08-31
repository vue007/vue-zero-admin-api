package com.zero.admin.web.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 登录业务指标。标签保持低基数，禁止加入租户号、用户名等业务标识。
 *
 * @author Akai
 */
@Component
public class LoginMetrics {

    private static final Set<String> KNOWN_GRANT_TYPES = Set.of("password", "sms", "email", "xcx", "social");

    private final MeterRegistry meterRegistry;

    public LoginMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public LoginAttempt start(String grantType) {
        return new LoginAttempt(meterRegistry, normalizeGrantType(grantType));
    }

    private String normalizeGrantType(String grantType) {
        return grantType != null && KNOWN_GRANT_TYPES.contains(grantType) ? grantType : "unknown";
    }

    public static final class LoginAttempt implements AutoCloseable {

        private final MeterRegistry meterRegistry;
        private final String grantType;
        private final Timer.Sample sample;
        private String outcome = "failure";

        private LoginAttempt(MeterRegistry meterRegistry, String grantType) {
            this.meterRegistry = meterRegistry;
            this.grantType = grantType;
            this.sample = Timer.start(meterRegistry);
        }

        public void success() {
            outcome = "success";
        }

        @Override
        public void close() {
            Counter.builder("zero.auth.login.attempts")
                .description("Number of login attempts")
                .tag("grant.type", grantType)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
            sample.stop(Timer.builder("zero.auth.login.duration")
                .description("Login request processing time")
                .tag("grant.type", grantType)
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
        }
    }
}

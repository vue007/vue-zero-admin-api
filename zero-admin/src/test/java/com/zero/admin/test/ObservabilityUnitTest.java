package com.zero.admin.test;

import com.zero.admin.base.web.filter.RequestIdFilter;
import com.zero.admin.web.service.LoginMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("dev")
class ObservabilityUnitTest {

    @Test
    void requestIdIsPropagatedToResponseAndMdcThenCleanedUp() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/system/user/list");
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "gateway-req-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInsideChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
            requestIdInsideChain.set(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY));

        filter.doFilter(request, response, chain);

        assertEquals("gateway-req-123", requestIdInsideChain.get());
        assertEquals("gateway-req-123", response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY));
    }

    @Test
    void unsafeRequestIdIsReplacedWithBoundedGeneratedValue() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "invalid request id with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        String generated = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertNotNull(generated);
        assertNotEquals("invalid request id with spaces", generated);
        assertEquals(32, generated.length());
        assertNull(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY));
    }

    @Test
    void loginMetricsRecordOnlyBoundedGrantTypeAndOutcomeTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        LoginMetrics metrics = new LoginMetrics(registry);

        try (LoginMetrics.LoginAttempt attempt = metrics.start("password")) {
            attempt.success();
        }
        try (LoginMetrics.LoginAttempt ignored = metrics.start("tenant-specific-custom-grant")) {
            // 默认记录为失败
        }

        Counter success = registry.find("zero.auth.login.attempts")
            .tags("grant.type", "password", "outcome", "success")
            .counter();
        Counter normalizedFailure = registry.find("zero.auth.login.attempts")
            .tags("grant.type", "unknown", "outcome", "failure")
            .counter();
        Timer successTimer = registry.find("zero.auth.login.duration")
            .tags("grant.type", "password", "outcome", "success")
            .timer();

        assertNotNull(success);
        assertNotNull(normalizedFailure);
        assertNotNull(successTimer);
        assertEquals(1.0, success.count());
        assertEquals(1.0, normalizedFailure.count());
        assertEquals(1, successTimer.count());
    }
}

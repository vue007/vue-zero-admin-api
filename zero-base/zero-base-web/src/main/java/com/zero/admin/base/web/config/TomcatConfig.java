package com.zero.admin.base.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Tomcat Web 容器安全配置。
 *
 * @author Akai
 */
@AutoConfiguration
public class TomcatConfig {

    private static final Set<String> DISALLOWED_HTTP_METHODS = Set.of("CONNECT", "TRACE", "TRACK");

    /**
     * 拒绝不安全的 HTTP 方法，保持原 Undertow 配置的安全行为。
     */
    @Bean
    public OncePerRequestFilter disallowedHttpMethodsFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws IOException, ServletException {
                if (DISALLOWED_HTTP_METHODS.contains(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}

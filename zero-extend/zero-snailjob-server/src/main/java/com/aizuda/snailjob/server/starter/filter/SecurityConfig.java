package com.aizuda.snailjob.server.starter.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * SnailJob Actuator 安全配置。
 *
 * @author Akai
 */
@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<ActuatorAuthFilter> actuatorFilter(
        @Value("${spring.boot.admin.client.username}") String username,
        @Value("${spring.boot.admin.client.password}") String password) {
        FilterRegistrationBean<ActuatorAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ActuatorAuthFilter(username, password));
        registration.addUrlPatterns("/actuator", "/actuator/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}

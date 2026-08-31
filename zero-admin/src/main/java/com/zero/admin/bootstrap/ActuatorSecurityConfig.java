package com.zero.admin.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 注册 Actuator 专用认证过滤器。
 *
 * @author Akai
 */
@Configuration
public class ActuatorSecurityConfig {

    @Bean
    public FilterRegistrationBean<ActuatorBasicAuthFilter> actuatorBasicAuthFilter(
        @Value("${monitor.actuator.username}") String username,
        @Value("${monitor.actuator.password}") String password) {
        FilterRegistrationBean<ActuatorBasicAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ActuatorBasicAuthFilter(username, password));
        registration.addUrlPatterns("/actuator", "/actuator/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}

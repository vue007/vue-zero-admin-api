package com.zero.admin.monitor.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * 监控中心认证与 iframe 安全配置。
 *
 * @author Akai
 */
@Configuration
public class MonitorSecurityConfig {

    private final String adminContextPath;

    public MonitorSecurityConfig(AdminServerProperties properties) {
        this.adminContextPath = properties.getContextPath();
    }

    @Bean
    public SecurityFilterChain monitorSecurityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminContextPath + "/");
        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();

        return http
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    paths.matcher(adminContextPath + "/assets/**"),
                    paths.matcher(adminContextPath + "/login")
                ).permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage(adminContextPath + "/login")
                .successHandler(successHandler))
            .logout(logout -> logout.logoutUrl(adminContextPath + "/logout"))
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}

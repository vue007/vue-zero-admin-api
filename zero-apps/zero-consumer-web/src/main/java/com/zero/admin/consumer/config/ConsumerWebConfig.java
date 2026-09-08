package com.zero.admin.consumer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** C 端接口身份隔离。 */
@Configuration
@RequiredArgsConstructor
public class ConsumerWebConfig implements WebMvcConfigurer {

    private final ConsumerIdentityInterceptor identityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(identityInterceptor)
            .addPathPatterns("/app/**")
            .excludePathPatterns(
                "/app/auth/register",
                "/app/auth/login/**",
                "/app/auth/social/authorize/**",
                "/app/auth/social/providers"
            );
    }
}

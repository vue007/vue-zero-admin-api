package com.zero.admin.base.shiro.config;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import com.zero.admin.base.shiro.realm.UserRealm;
import com.zero.admin.base.shiro.session.ShiroRedisSessionDAO;
import com.zero.admin.base.shiro.session.TokenWebSessionManager;
import com.zero.admin.base.shiro.web.RestAuthFilter;

/**
 * Apache Shiro 配置。
 * <p>
 * 采用「全局 rest 过滤器认证 + 注解授权」的方式：
 * 公开路径使用 {@code anon}，其余路径默认要求登录（返回 JSON 401），
 * 权限与角色通过 {@code @RequiresPermissions}/{@code @RequiresRoles} 注解校验。
 *
 * @author Akai
 */
@AutoConfiguration
public class ShiroConfig {

    @Bean
    public Realm userRealm() {
        return new UserRealm();
    }

    @Bean
    public ShiroRedisSessionDAO shiroSessionDAO() {
        return new ShiroRedisSessionDAO();
    }

    @Bean
    public DefaultWebSessionManager sessionManager(ShiroRedisSessionDAO sessionDAO) {
        TokenWebSessionManager sessionManager = new TokenWebSessionManager();
        sessionManager.setSessionDAO(sessionDAO);
        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(Realm realm, DefaultWebSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    public RestAuthFilter restAuthFilter() {
        return new RestAuthFilter();
    }

    /**
     * 禁止 Spring Boot 将 {@code restAuthFilter} 作为全局 Servlet 过滤器自动注册，
     * 使其只作为 Shiro 过滤链中的一环生效，避免误拦截 {@code anon} 公开路径（如登录、验证码）。
     */
    @Bean
    public FilterRegistrationBean<RestAuthFilter> restAuthFilterRegistration(RestAuthFilter restAuthFilter) {
        FilterRegistrationBean<RestAuthFilter> registration = new FilterRegistrationBean<>(restAuthFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        // 公开访问路径
        chainDefinition.addPathDefinition("/", "anon");
        chainDefinition.addPathDefinition("/auth/login", "anon");
        chainDefinition.addPathDefinition("/auth/captcha", "anon");
        chainDefinition.addPathDefinition("/auth/register", "anon");
        chainDefinition.addPathDefinition("/auth/tenant/list", "anon");
        chainDefinition.addPathDefinition("/auth/binding/**", "anon");
        // Actuator 由 zero-admin 中的 Basic Auth 过滤器单独保护。
        chainDefinition.addPathDefinition("/actuator", "anon");
        chainDefinition.addPathDefinition("/actuator/**", "anon");

        // OpenAPI 描述与 Scalar 文档界面
        chainDefinition.addPathDefinition("/v3/api-docs", "anon");
        chainDefinition.addPathDefinition("/v3/api-docs/**", "anon");
        chainDefinition.addPathDefinition("/scalar", "anon");
        chainDefinition.addPathDefinition("/scalar/**", "anon");
        // 其余路径均需登录
        chainDefinition.addPathDefinition("/**", "restAuthFilter");
        return chainDefinition;
    }

}

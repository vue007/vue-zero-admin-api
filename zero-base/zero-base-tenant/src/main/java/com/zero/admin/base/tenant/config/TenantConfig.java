package com.zero.admin.base.tenant.config;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.zero.admin.base.redis.config.RedisConfig;
import com.zero.admin.base.redis.config.properties.RedissonProperties;
import com.zero.admin.base.tenant.handle.PlusTenantLineHandler;
import com.zero.admin.base.tenant.handle.TenantKeyPrefixHandler;
import com.zero.admin.base.tenant.manager.TenantSpringCacheManager;
import com.zero.admin.base.tenant.properties.TenantProperties;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 租户配置类
 *
 * @author Akai
 */
@EnableConfigurationProperties(TenantProperties.class)
@AutoConfiguration(after = {RedisConfig.class})
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class TenantConfig {

    @ConditionalOnClass(TenantLineInnerInterceptor.class)
    @AutoConfiguration
    static class MybatisPlusConfiguration {

        /**
         * 多租户插件
         */
        @Bean
        public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantProperties tenantProperties) {
            return new TenantLineInnerInterceptor(new PlusTenantLineHandler(tenantProperties));
        }

    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public RedissonAutoConfigurationCustomizer tenantRedissonCustomizer(RedissonProperties redissonProperties) {
        // Redisson 4 将名称映射提升为全局配置；租户映射最后执行并覆盖基础前缀映射。
        return config -> config.setNameMapper(new TenantKeyPrefixHandler(redissonProperties.getKeyPrefix()));
    }

    /**
     * 多租户缓存管理器
     */
    @Primary
    @Bean
    public CacheManager tenantCacheManager() {
        return new TenantSpringCacheManager();
    }

}

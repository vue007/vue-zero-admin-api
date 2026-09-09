package com.zero.admin.base.social.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Social 配置属性
 *
 * @author Akai
 */
@Data
@Component
@ConfigurationProperties(prefix = "justauth")
public class SocialProperties {

    /**
     * 第三方平台 HTTP 请求配置
     */
    private HttpConfig httpConfig = new HttpConfig();

    /**
     * 授权类型
     */
    private Map<String, SocialLoginConfigProperties> type;

    /**
     * 第三方平台 HTTP 请求配置
     */
    @Data
    public static class HttpConfig {

        /**
         * 连接与读取超时时间（毫秒）
         */
        private int timeout = 30000;

        /**
         * 按 source 配置的代理
         */
        private Map<String, ProxyConfig> proxy = new HashMap<>();

    }

    /**
     * 第三方平台代理配置
     */
    @Data
    public static class ProxyConfig {

        /**
         * 代理类型
         */
        private Proxy.Type type = Proxy.Type.HTTP;

        /**
         * 代理主机
         */
        private String hostname;

        /**
         * 代理端口
         */
        private Integer port;

    }

}

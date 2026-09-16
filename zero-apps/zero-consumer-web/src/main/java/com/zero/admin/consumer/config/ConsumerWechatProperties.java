package com.zero.admin.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** 微信小程序配置。密钥应通过环境变量或未跟踪的 secret 文件提供。 */
@Data
@Component
@ConfigurationProperties(prefix = "consumer.wechat")
public class ConsumerWechatProperties {

    /** key 为平台 app_application.app_id，避免应用跨租户选择微信配置。 */
    private Map<String, MiniProgram> miniPrograms = new HashMap<>();

    @Data
    public static class MiniProgram {
        /** 微信公众平台的小程序 AppID。 */
        private String wechatAppId;

        private String secret;
    }
}

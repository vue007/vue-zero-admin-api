package com.zero.admin.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/** C 端认证边界配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "consumer.auth")
public class ConsumerAuthProperties {

    /** C 端服务绑定的租户，禁止由调用方任意切换。 */
    private String tenantId = "000000";

    /** 允许 C 端使用的 sys_client.client_key。 */
    private List<String> allowedClientKeys = List.of("app");

    /** 密码连续失败次数上限。 */
    private int maxRetryCount = 5;

    /** 达到失败上限后的锁定分钟数。 */
    private int lockMinutes = 10;
}

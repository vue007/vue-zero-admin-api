package com.zero.admin.tenantapp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** App 与终端渠道校验通过后的可信认证上下文。 */
@Data
@AllArgsConstructor
public class TenantApplicationClientAuthVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private String tenantId;
    private String appId;
    private List<String> scopes;

    private Long authClientId;
    private String clientId;
    private String clientKey;
    private String deviceType;
    private String grantType;
    private Long timeout;
    private Long activeTimeout;
    private String channel;
}

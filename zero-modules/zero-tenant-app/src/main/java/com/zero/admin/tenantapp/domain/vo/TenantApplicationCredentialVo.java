package com.zero.admin.tenantapp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 仅在创建或重置密钥成功时返回一次的凭证。 */
@Data
@AllArgsConstructor
public class TenantApplicationCredentialVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantId;
    private String appId;
    private String appSecret;
}

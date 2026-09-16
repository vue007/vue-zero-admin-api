package com.zero.admin.tenantapp.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** App 可绑定的认证客户端选项，不包含客户端密钥。 */
@Data
public class TenantApplicationClientOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long authClientId;
    private String clientKey;
    private String deviceType;
    private String grantType;
    private List<String> grantTypeList;
    private Long timeout;
    private Long activeTimeout;
    private String clientStatus;
}

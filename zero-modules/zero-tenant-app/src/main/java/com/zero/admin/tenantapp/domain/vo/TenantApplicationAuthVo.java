package com.zero.admin.tenantapp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** App 请求通过接入校验后可使用的可信租户上下文。 */
@Data
@AllArgsConstructor
public class TenantApplicationAuthVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantId;
    private String appId;
    private String appType;
    private List<String> scopes;
}

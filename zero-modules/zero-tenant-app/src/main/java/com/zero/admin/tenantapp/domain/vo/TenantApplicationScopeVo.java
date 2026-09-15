package com.zero.admin.tenantapp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/** App 管理下可授权的业务模块。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantApplicationScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 保存到应用凭证的模块级授权标识。 */
    private String value;

    /** 界面上展示的 App 管理模块名称。 */
    private String label;
}

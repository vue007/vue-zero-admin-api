package com.zero.admin.tenantapp.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** App 接入状态修改对象。 */
@Data
public class TenantApplicationStatusBo {

    @NotNull(message = "应用ID不能为空")
    private Long id;

    @NotBlank(message = "应用状态不能为空")
    @Pattern(regexp = "[01]", message = "应用状态只能为0或1")
    private String status;
}

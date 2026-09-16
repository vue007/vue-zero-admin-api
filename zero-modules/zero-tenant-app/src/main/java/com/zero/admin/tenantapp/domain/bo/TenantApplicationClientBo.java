package com.zero.admin.tenantapp.domain.bo;

import com.zero.admin.base.core.validate.AddGroup;
import com.zero.admin.base.core.validate.EditGroup;
import com.zero.admin.tenantapp.domain.TenantApplicationClient;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** App 终端认证策略绑定维护对象。 */
@Data
@AutoMapper(target = TenantApplicationClient.class, reverseConvertGenerate = false)
public class TenantApplicationClientBo {

    @NotNull(message = "认证客户端不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long authClientId;

    @NotBlank(message = "终端渠道不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "终端渠道长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class})
    @Pattern(
        regexp = "[A-Za-z][A-Za-z0-9_-]{0,31}",
        message = "终端渠道只能包含字母、数字、下划线或短横线，且必须以字母开头",
        groups = {AddGroup.class, EditGroup.class}
    )
    private String channel;

    @NotBlank(message = "终端状态不能为空", groups = {AddGroup.class, EditGroup.class})
    @Pattern(regexp = "[01]", message = "终端状态只能为0或1",
        groups = {AddGroup.class, EditGroup.class})
    private String status;
}

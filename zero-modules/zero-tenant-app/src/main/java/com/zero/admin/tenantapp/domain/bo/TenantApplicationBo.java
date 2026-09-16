package com.zero.admin.tenantapp.domain.bo;

import com.zero.admin.base.core.validate.AddGroup;
import com.zero.admin.base.core.validate.EditGroup;
import com.zero.admin.base.core.xss.Xss;
import com.zero.admin.base.mybatis.core.domain.BaseEntity;
import com.zero.admin.tenantapp.domain.TenantApplication;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 租户 App 接入查询及维护对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TenantApplication.class, reverseConvertGenerate = false)
public class TenantApplicationBo extends BaseEntity {

    @NotNull(message = "应用ID不能为空", groups = EditGroup.class)
    private Long id;

    /** 仅平台创建及平台筛选使用；租户自助接口不会信任该字段。 */
    @Size(max = 20, message = "租户编号长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class})
    private String tenantId;

    @Xss(message = "应用名称不能包含脚本字符",
        groups = {AddGroup.class, EditGroup.class})
    @NotBlank(message = "应用名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "应用名称长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class})
    private String appName;

    /** 只用于列表筛选，创建时由服务端生成，修改时不可变。 */
    @Size(max = 64, message = "App ID长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class})
    private String appId;

    @NotNull(message = "授权范围不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 1, max = 32, message = "授权范围必须选择1至{max}项",
        groups = {AddGroup.class, EditGroup.class})
    private List<
        @NotBlank(message = "授权范围不能为空", groups = {AddGroup.class, EditGroup.class})
        @Size(max = 64, message = "授权范围长度不能超过{max}个字符",
            groups = {AddGroup.class, EditGroup.class})
        @Pattern(
            regexp = "[A-Za-z][A-Za-z0-9:_*.-]{0,63}",
            message = "授权范围格式不正确",
            groups = {AddGroup.class, EditGroup.class}
        ) String> scopes;

    @NotNull(message = "至少需要配置一个终端", groups = {AddGroup.class, EditGroup.class})
    @Size(min = 1, max = 16, message = "终端数量必须为1至{max}个",
        groups = {AddGroup.class, EditGroup.class})
    private List<@jakarta.validation.Valid TenantApplicationClientBo> terminals;

    @Pattern(regexp = "[01]", message = "应用状态只能为0或1",
        groups = {AddGroup.class, EditGroup.class})
    private String status;

    @Size(max = 500, message = "备注长度不能超过{max}个字符",
        groups = {AddGroup.class, EditGroup.class})
    private String remark;
}

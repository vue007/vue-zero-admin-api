package com.zero.admin.web.controller.tenant;

import com.zero.admin.base.core.constant.TenantConstants;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.core.validate.AddGroup;
import com.zero.admin.base.core.validate.EditGroup;
import com.zero.admin.base.idempotent.annotation.RepeatSubmit;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationBo;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationStatusBo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationCredentialVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationScopeVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.tenantapp.service.ITenantApplicationService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 平台管理员跨租户 App 接入管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequiresRoles(TenantConstants.SUPER_ADMIN_ROLE_KEY)
@RequestMapping("/system/tenant-app")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantAppController extends BaseController {

    private final ITenantApplicationService applicationService;

    @RequiresPermissions("system:tenantApp:list")
    @GetMapping("/list")
    public TableDataInfo<TenantApplicationVo> list(
        TenantApplicationBo bo, PageQuery pageQuery) {
        return applicationService.queryAllTenantPage(bo, pageQuery);
    }

    @RequiresPermissions("system:tenantApp:list")
    @GetMapping("/scope-options")
    public R<List<TenantApplicationScopeVo>> scopeOptions() {
        return R.ok(applicationService.queryScopeOptions());
    }

    @RequiresPermissions("system:tenantApp:query")
    @GetMapping("/{id}")
    public R<TenantApplicationVo> getInfo(
        @NotNull(message = "应用ID不能为空") @PathVariable Long id) {
        return R.ok(applicationService.queryAllTenantById(id));
    }

    @RequiresPermissions("system:tenantApp:add")
    @Log(
        title = "App接入管理",
        businessType = BusinessType.INSERT,
        isSaveResponseData = false
    )
    @RepeatSubmit
    @PostMapping
    public R<TenantApplicationCredentialVo> add(
        @Validated(AddGroup.class) @RequestBody TenantApplicationBo bo) {
        return R.ok(applicationService.createForTenant(bo));
    }

    @RequiresPermissions("system:tenantApp:edit")
    @Log(title = "App接入管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(
        @Validated(EditGroup.class) @RequestBody TenantApplicationBo bo) {
        return toAjax(applicationService.updateForTenant(bo));
    }

    @RequiresPermissions("system:tenantApp:status")
    @Log(title = "App接入管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(
        @Validated @RequestBody TenantApplicationStatusBo bo) {
        return toAjax(applicationService.changeStatusForTenant(bo));
    }

    @RequiresPermissions("system:tenantApp:resetSecret")
    @Log(
        title = "App接入管理-重置密钥",
        businessType = BusinessType.UPDATE,
        isSaveResponseData = false
    )
    @RepeatSubmit
    @PostMapping("/{id}/reset-secret")
    public R<TenantApplicationCredentialVo> resetSecret(
        @NotNull(message = "应用ID不能为空") @PathVariable Long id) {
        return R.ok(applicationService.resetSecretForTenant(id));
    }

    @RequiresPermissions("system:tenantApp:remove")
    @Log(title = "App接入管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(
        @NotEmpty(message = "应用ID不能为空") @PathVariable Long[] ids) {
        return toAjax(applicationService.deleteForTenant(List.of(ids)));
    }
}

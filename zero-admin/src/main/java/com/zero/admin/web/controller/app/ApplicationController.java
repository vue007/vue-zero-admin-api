package com.zero.admin.web.controller.app;

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
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientOptionVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationScopeVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.tenantapp.service.ITenantApplicationService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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

/** 当前租户的应用接入自助管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/application")
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class ApplicationController extends BaseController {

    private final ITenantApplicationService applicationService;

    @RequiresPermissions("app:application:list")
    @GetMapping("/list")
    public TableDataInfo<TenantApplicationVo> list(
        TenantApplicationBo bo, PageQuery pageQuery) {
        bo.setTenantId(null);
        return applicationService.queryCurrentTenantPage(bo, pageQuery);
    }

    @RequiresPermissions("app:application:list")
    @GetMapping("/scope-options")
    public R<List<TenantApplicationScopeVo>> scopeOptions() {
        return R.ok(applicationService.queryScopeOptions());
    }

    @RequiresPermissions("app:application:list")
    @GetMapping("/client-options")
    public R<List<TenantApplicationClientOptionVo>> clientOptions() {
        return R.ok(applicationService.queryClientOptions());
    }

    @RequiresPermissions("app:application:query")
    @GetMapping("/{id}")
    public R<TenantApplicationVo> getInfo(
        @NotNull(message = "应用ID不能为空") @PathVariable Long id) {
        return R.ok(applicationService.queryCurrentTenantById(id));
    }

    @RequiresPermissions("app:application:add")
    @Log(
        title = "应用接入",
        businessType = BusinessType.INSERT,
        isSaveResponseData = false
    )
    @RepeatSubmit
    @PostMapping
    public R<TenantApplicationCredentialVo> add(
        @Validated(AddGroup.class) @RequestBody TenantApplicationBo bo) {
        bo.setTenantId(null);
        return R.ok(applicationService.createForCurrentTenant(bo));
    }

    @RequiresPermissions("app:application:edit")
    @Log(title = "应用接入", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(
        @Validated(EditGroup.class) @RequestBody TenantApplicationBo bo) {
        bo.setTenantId(null);
        return toAjax(applicationService.updateForCurrentTenant(bo));
    }

    @RequiresPermissions("app:application:status")
    @Log(title = "应用接入", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(
        @Validated @RequestBody TenantApplicationStatusBo bo) {
        return toAjax(applicationService.changeStatusForCurrentTenant(bo));
    }

    @RequiresPermissions("app:application:resetSecret")
    @Log(
        title = "应用接入-重置密钥",
        businessType = BusinessType.UPDATE,
        isSaveResponseData = false
    )
    @RepeatSubmit
    @PostMapping("/{id}/reset-secret")
    public R<TenantApplicationCredentialVo> resetSecret(
        @NotNull(message = "应用ID不能为空") @PathVariable Long id) {
        return R.ok(applicationService.resetSecretForCurrentTenant(id));
    }

    @RequiresPermissions("app:application:remove")
    @Log(title = "应用接入", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(
        @NotEmpty(message = "应用ID不能为空") @PathVariable Long[] ids) {
        return toAjax(applicationService.deleteForCurrentTenant(List.of(ids)));
    }
}

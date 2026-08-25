package com.zero.admin.system.controller;

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
import com.zero.admin.system.domain.bo.SysOssConfigBo;
import com.zero.admin.system.domain.vo.SysOssConfigVo;
import com.zero.admin.system.service.ISysOssConfigService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
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

/**
 * OSS 存储配置管理。配置为全局共享数据，仅超级管理员可维护。
 *
 * @author Akai
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequiresRoles(TenantConstants.SUPER_ADMIN_ROLE_KEY)
@RequestMapping("/system/oss/config")
public class SysOssConfigController extends BaseController {

    private final ISysOssConfigService ossConfigService;

    /**
     * 查询存储配置列表。
     */
    @RequiresPermissions("system:ossConfig:list")
    @GetMapping("/list")
    public TableDataInfo<SysOssConfigVo> list(SysOssConfigBo bo, PageQuery pageQuery) {
        return ossConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询存储配置详情。SecretKey 不会返回到浏览器。
     */
    @RequiresPermissions("system:ossConfig:list")
    @GetMapping("/{ossConfigId}")
    public R<SysOssConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long ossConfigId) {
        return R.ok(ossConfigService.queryById(ossConfigId));
    }

    /**
     * 新增存储配置。
     */
    @RequiresPermissions("system:ossConfig:add")
    @Log(title = "OSS存储配置", businessType = BusinessType.INSERT,
        excludeParamNames = {"accessKey", "secretKey"})
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOssConfigBo bo) {
        return toAjax(ossConfigService.insertByBo(bo));
    }

    /**
     * 修改存储配置；SecretKey 为空时保留原值。
     */
    @RequiresPermissions("system:ossConfig:edit")
    @Log(title = "OSS存储配置", businessType = BusinessType.UPDATE,
        excludeParamNames = {"accessKey", "secretKey"})
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysOssConfigBo bo) {
        return toAjax(ossConfigService.updateByBo(bo));
    }

    /**
     * 设置默认存储配置。
     */
    @RequiresPermissions("system:ossConfig:edit")
    @Log(title = "OSS存储配置", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysOssConfigBo bo) {
        return toAjax(ossConfigService.updateOssConfigStatus(bo));
    }

    /**
     * 删除存储配置。
     */
    @RequiresPermissions("system:ossConfig:remove")
    @Log(title = "OSS存储配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ossConfigIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossConfigIds) {
        return toAjax(ossConfigService.deleteWithValidByIds(List.of(ossConfigIds), true));
    }
}

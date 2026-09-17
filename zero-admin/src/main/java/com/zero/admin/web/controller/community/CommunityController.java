package com.zero.admin.web.controller.community;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.community.domain.bo.CommunityAssetQueryBo;
import com.zero.admin.community.domain.bo.CommunityFeatureBo;
import com.zero.admin.community.domain.bo.CommunityReviewBo;
import com.zero.admin.community.domain.vo.CommunityAssetVo;
import com.zero.admin.community.service.ICommunityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

/** 后台社区审核与运营接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/community")
public class CommunityController {

    private final ICommunityService communityService;

    @RequiresPermissions("app:community:list")
    @GetMapping("/list")
    public TableDataInfo<CommunityAssetVo> list(CommunityAssetQueryBo bo, PageQuery pageQuery) {
        return withAuthorizedTenantScope(() -> communityService.queryAdminPage(bo, pageQuery));
    }

    @RequiresPermissions("app:community:query")
    @GetMapping("/{assetId}")
    public R<CommunityAssetVo> detail(@NotNull @PathVariable Long assetId) {
        return R.ok(withAuthorizedTenantScope(() -> communityService.getAdminDetail(assetId)));
    }

    @RequiresPermissions("app:community:review")
    @Log(title = "社区内容审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{assetId}/review")
    public R<Void> review(@NotNull @PathVariable Long assetId, @Valid @RequestBody CommunityReviewBo bo) {
        withAuthorizedTenantScope(() -> {
            communityService.review(assetId, bo);
            return Boolean.TRUE;
        });
        return R.ok();
    }

    @RequiresPermissions("app:community:feature")
    @Log(title = "社区内容精选", businessType = BusinessType.UPDATE)
    @PutMapping("/{assetId}/feature")
    public R<Void> feature(@NotNull @PathVariable Long assetId, @Valid @RequestBody CommunityFeatureBo bo) {
        withAuthorizedTenantScope(() -> {
            communityService.feature(assetId, Boolean.TRUE.equals(bo.getFeatured()));
            return Boolean.TRUE;
        });
        return R.ok();
    }

    @RequiresPermissions("app:community:hide")
    @Log(title = "社区内容下架", businessType = BusinessType.UPDATE)
    @PutMapping("/{assetId}/hide")
    public R<Void> hide(@NotNull @PathVariable Long assetId) {
        withAuthorizedTenantScope(() -> {
            communityService.hide(assetId);
            return Boolean.TRUE;
        });
        return R.ok();
    }

    private <T> T withAuthorizedTenantScope(Supplier<T> action) {
        if (LoginHelper.isSuperAdmin() && TenantHelper.getDynamic() == null) {
            return TenantHelper.ignore(action);
        }
        return action.get();
    }
}

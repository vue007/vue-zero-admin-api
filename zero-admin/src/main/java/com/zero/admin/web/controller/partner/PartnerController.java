package com.zero.admin.web.controller.partner;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.partner.domain.bo.PartnerBo;
import com.zero.admin.partner.domain.vo.PartnerVo;
import com.zero.admin.partner.service.IPartnerService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前租户的 App 合作客户管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/partner")
public class PartnerController extends BaseController {

    private final IPartnerService partnerService;

    @RequiresPermissions("app:partner:list")
    @GetMapping("/list")
    public TableDataInfo<PartnerVo> list(PartnerBo bo, PageQuery pageQuery) {
        return partnerService.queryPageList(bo, pageQuery);
    }

    @RequiresPermissions("app:partner:query")
    @GetMapping("/{partnerId}")
    public R<PartnerVo> getInfo(
        @NotNull(message = "合作客户ID不能为空") @PathVariable Long partnerId) {
        return R.ok(partnerService.queryById(partnerId));
    }

    @RequiresPermissions("app:partner:add")
    @Log(title = "合作客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody PartnerBo bo) {
        return toAjax(partnerService.insertPartner(bo));
    }

    @RequiresPermissions("app:partner:edit")
    @Log(title = "合作客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody PartnerBo bo) {
        return toAjax(partnerService.updatePartner(bo));
    }

    @RequiresPermissions("app:partner:remove")
    @Log(title = "合作客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{partnerIds}")
    public R<Void> remove(@PathVariable Long[] partnerIds) {
        return toAjax(partnerService.deletePartnerByIds(partnerIds));
    }
}

package com.zero.admin.web.controller.member;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.member.domain.bo.MemberQueryBo;
import com.zero.admin.member.domain.bo.MemberStatusBo;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.service.IMemberService;
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

/** 管理后台会员管理接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/member")
public class MemberController {

    private final IMemberService memberService;

    @RequiresPermissions("system:member:list")
    @GetMapping("/list")
    public TableDataInfo<MemberVo> list(MemberQueryBo bo, PageQuery pageQuery) {
        return memberService.queryPageList(bo, pageQuery);
    }

    @RequiresPermissions("system:member:query")
    @GetMapping("/{memberId}")
    public R<MemberVo> getInfo(
        @NotNull(message = "会员ID不能为空") @PathVariable Long memberId) {
        return R.ok(memberService.queryById(memberId));
    }

    @RequiresPermissions("system:member:edit")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@Valid @RequestBody MemberStatusBo bo) {
        return memberService.updateStatus(bo.getMemberId(), bo.getStatus())
            ? R.ok() : R.fail("修改会员状态失败");
    }
}

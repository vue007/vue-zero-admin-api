package com.zero.admin.consumer.controller;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.consumer.domain.vo.ConsumerMemberProfileVo;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.service.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 会员本人接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/member")
public class ConsumerProfileController {

    private final IMemberService memberService;

    @GetMapping("/profile")
    public R<ConsumerMemberProfileVo> profile() {
        MemberVo member = memberService.queryById(LoginHelper.getUserId());
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        return R.ok(ConsumerMemberProfileVo.from(member));
    }
}

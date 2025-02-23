package com.zero.admin.system.controller;

import lombok.RequiredArgsConstructor;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.satoken.utils.LoginHelper;
import com.zero.admin.base.web.core.BaseController;
import com.zero.admin.system.domain.vo.SysSocialVo;
import com.zero.admin.system.service.ISysSocialService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社会化关系
 *
 * @author Akai
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/social")
public class SysSocialController extends BaseController {

    private final ISysSocialService socialUserService;

    /**
     * 查询社会化关系列表
     */
    @GetMapping("/list")
    public R<List<SysSocialVo>> list() {
        return R.ok(socialUserService.queryListByUserId(LoginHelper.getUserId()));
    }

}

package com.zero.admin.consumer.controller;

import com.zero.admin.base.core.domain.R;
import com.zero.admin.consumer.domain.request.ConsumerPasswordLoginRequest;
import com.zero.admin.consumer.domain.request.ConsumerRegisterRequest;
import com.zero.admin.consumer.domain.request.ConsumerSocialLoginRequest;
import com.zero.admin.consumer.domain.request.ConsumerWechatLoginRequest;
import com.zero.admin.consumer.domain.vo.ConsumerLoginVo;
import com.zero.admin.consumer.service.ConsumerAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** C 端认证接口，与后台 /auth 认证体系分离。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/auth")
public class ConsumerAuthController {

    private final ConsumerAuthService authService;

    @PostMapping("/register")
    public R<ConsumerLoginVo> register(@Valid @RequestBody ConsumerRegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @PostMapping("/login/password")
    public R<ConsumerLoginVo> passwordLogin(@Valid @RequestBody ConsumerPasswordLoginRequest request) {
        return R.ok(authService.passwordLogin(request));
    }

    @PostMapping("/login/social")
    public R<ConsumerLoginVo> socialLogin(@Valid @RequestBody ConsumerSocialLoginRequest request) {
        return R.ok(authService.socialLogin(request));
    }

    @PostMapping("/login/wechat-mini-program")
    public R<ConsumerLoginVo> wechatMiniProgramLogin(
        @Valid @RequestBody ConsumerWechatLoginRequest request) {
        return R.ok(authService.wechatMiniProgramLogin(request));
    }

    @GetMapping("/social/authorize/{source}")
    public R<String> socialAuthorizeUrl(
        @PathVariable @NotBlank String source,
        @RequestParam @NotBlank String clientId) {
        return R.ok(authService.socialAuthorizeUrl(clientId, source));
    }

    @GetMapping("/social/providers")
    public R<List<String>> socialProviders() {
        return R.ok(authService.socialProviders());
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok("退出成功");
    }
}

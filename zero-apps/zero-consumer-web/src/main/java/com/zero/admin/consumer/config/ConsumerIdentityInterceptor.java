package com.zero.admin.consumer.config;

import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.member.domain.model.MemberLoginUser;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationAuthVo;
import com.zero.admin.tenantapp.service.ITenantApplicationService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/** 校验 C 端会话身份，并让应用、租户或 scope 的停用立即生效。 */
@Component
@RequiredArgsConstructor
public class ConsumerIdentityInterceptor implements HandlerInterceptor {

    private static final String MEMBER_SCOPE = "app:member";

    private final ConsumerAuthProperties authProperties;
    private final ITenantApplicationService applicationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (!(loginUser instanceof MemberLoginUser memberLoginUser)) {
            throw invalidSession();
        }
        if (!UserType.APP_USER.getUserType().equals(loginUser.getUserType())
            || StringUtils.isBlank(loginUser.getTenantId())
            || memberLoginUser.getApplicationId() == null
            || StringUtils.isBlank(memberLoginUser.getAppId())
            || authProperties.getAllowedClientKeys() == null
            || authProperties.getAllowedClientKeys().stream()
                .noneMatch(key -> key.equalsIgnoreCase(loginUser.getClientKey()))) {
            throw invalidSession();
        }

        TenantApplicationAuthVo application;
        try {
            application = applicationService.resolveEnabledByAppId(memberLoginUser.getAppId());
        } catch (RuntimeException exception) {
            throw invalidSession();
        }
        if (!Objects.equals(application.getId(), memberLoginUser.getApplicationId())
            || !Objects.equals(application.getTenantId(), memberLoginUser.getTenantId())
            || application.getScopes() == null
            || application.getScopes().stream()
                .noneMatch(MEMBER_SCOPE::equalsIgnoreCase)) {
            throw invalidSession();
        }
        return true;
    }

    private UnauthorizedException invalidSession() {
        LoginHelper.logout();
        return new UnauthorizedException("当前会话不是有效的C端会员会话");
    }
}

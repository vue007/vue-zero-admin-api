package com.zero.admin.consumer.config;

import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.member.domain.model.MemberLoginUser;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientAuthVo;
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
            || memberLoginUser.getAuthClientId() == null
            || StringUtils.isBlank(memberLoginUser.getAppId())
            || StringUtils.isBlank(memberLoginUser.getChannel())
            || StringUtils.isBlank(loginUser.getClientKey())) {
            throw invalidSession();
        }

        TenantApplicationClientAuthVo applicationClient;
        try {
            applicationClient = applicationService.resolveEnabledClient(
                memberLoginUser.getAppId(), memberLoginUser.getChannel());
        } catch (RuntimeException exception) {
            throw invalidSession();
        }
        if (!Objects.equals(applicationClient.getApplicationId(), memberLoginUser.getApplicationId())
            || !Objects.equals(applicationClient.getAuthClientId(), memberLoginUser.getAuthClientId())
            || !Objects.equals(applicationClient.getTenantId(), memberLoginUser.getTenantId())
            || !applicationClient.getClientKey().equalsIgnoreCase(loginUser.getClientKey())
            || applicationClient.getScopes() == null
            || applicationClient.getScopes().stream()
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

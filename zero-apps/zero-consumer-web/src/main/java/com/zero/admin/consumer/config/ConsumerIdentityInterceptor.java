package com.zero.admin.consumer.config;

import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.shiro.utils.LoginHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 阻止后台 sys_user 会话访问 C 端受保护接口。 */
@Component
public class ConsumerIdentityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || !UserType.APP_USER.getUserType().equals(loginUser.getUserType())) {
            throw new UnauthorizedException("当前会话不是C端会员会话");
        }
        return true;
    }
}

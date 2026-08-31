package com.zero.admin.base.shiro.authc;

import org.apache.shiro.authc.AuthenticationToken;
import com.zero.admin.base.core.domain.model.LoginUser;

/**
 * 登录凭证，principal 直接携带已通过业务层校验的登录用户。
 * <p>
 * 密码已在 AuthStrategy 层通过 BCrypt 校验，Realm 使用
 * {@code AllowAllCredentialsMatcher} 放行凭证匹配，仅在此触发认证流程。
 *
 * @author Akai
 */
public class LoginUserToken implements AuthenticationToken {

    private final LoginUser loginUser;

    public LoginUserToken(LoginUser loginUser) {
        this.loginUser = loginUser;
    }

    public LoginUser getLoginUser() {
        return loginUser;
    }

    @Override
    public Object getPrincipal() {
        return loginUser;
    }

    @Override
    public Object getCredentials() {
        return loginUser.getUserId();
    }

}

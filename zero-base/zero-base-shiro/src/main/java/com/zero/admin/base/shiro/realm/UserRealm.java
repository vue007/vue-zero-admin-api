package com.zero.admin.base.shiro.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.shiro.authc.LoginUserToken;

/**
 * 用户认证与授权 Realm。
 * <p>
 * 密码校验已在登录业务层完成，此处不重复校验凭证；授权信息直接取自
 * {@link LoginUser} 中已加载好的菜单/角色权限标识。
 *
 * @author Akai
 */
public class UserRealm extends AuthorizingRealm {

    public UserRealm() {
        // 凭证已在业务层校验，放行 Shiro 的凭证匹配步骤。
        setCredentialsMatcher(new AllowAllCredentialsMatcher());
        // 默认仅支持 UsernamePasswordToken，此处声明支持自定义的 LoginUserToken。
        setAuthenticationTokenClass(LoginUserToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Object primary = principals.getPrimaryPrincipal();
        if (!(primary instanceof LoginUser loginUser)) {
            return info;
        }
        UserType userType = UserType.getUserType(loginUser.getUserType());
        if (userType == UserType.SYS_USER) {
            if (loginUser.getMenuPermission() != null) {
                info.addStringPermissions(loginUser.getMenuPermission());
            }
            if (loginUser.getRolePermission() != null) {
                info.addRoles(loginUser.getRolePermission());
            }
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        if (!(token instanceof LoginUserToken loginUserToken)) {
            return null;
        }
        LoginUser loginUser = loginUserToken.getLoginUser();
        return new SimpleAuthenticationInfo(loginUser, loginUser.getUserId(), getName());
    }

}

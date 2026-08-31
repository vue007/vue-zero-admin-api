package com.zero.admin.base.shiro.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.constant.TenantConstants;
import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.shiro.authc.LoginUserToken;
import com.zero.admin.base.shiro.session.ShiroSessionStore;

import java.util.Set;

/**
 * 登录鉴权助手（Shiro 实现）。
 *
 * @author Akai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    /**
     * 客户端标识请求头（用于登录日志记录时识别客户端）。
     */
    public static final String CLIENT_KEY = "clientid";

    /**
     * 登录系统。
     *
     * @param loginUser     登录用户信息
     * @param timeoutSeconds 会话有效期（秒），小于等于 0 表示使用全局默认值
     */
    public static void login(LoginUser loginUser, long timeoutSeconds) {
        Subject subject = SecurityUtils.getSubject();
        subject.login(new LoginUserToken(loginUser));
        if (timeoutSeconds > 0) {
            subject.getSession().setTimeout(timeoutSeconds * 1000L);
        }
    }

    /**
     * 登录系统（使用全局默认会话有效期）。
     */
    public static void login(LoginUser loginUser) {
        login(loginUser, 0L);
    }

    /**
     * 获取当前登录用户。
     */
    public static LoginUser getLoginUser() {
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 根据 token（会话 ID）获取登录用户。
     */
    public static LoginUser getLoginUser(String token) {
        Session session = ShiroSessionStore.read(token);
        if (session == null) {
            return null;
        }
        Object attribute = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
        if (attribute instanceof PrincipalCollection collection) {
            return (LoginUser) collection.getPrimaryPrincipal();
        }
        return null;
    }

    /**
     * 获取用户 id。
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取用户 id（字符串）。
     */
    public static String getUserIdStr() {
        return Convert.toStr(getUserId());
    }

    /**
     * 获取用户账户。
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    /**
     * 获取租户 ID。
     */
    public static String getTenantId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getTenantId();
    }

    /**
     * 获取部门 ID。
     */
    public static Long getDeptId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getDeptId();
    }

    /**
     * 获取部门名。
     */
    public static String getDeptName() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getDeptName();
    }

    /**
     * 获取部门类别编码。
     */
    public static String getDeptCategory() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getDeptCategory();
    }

    /**
     * 获取用户类型。
     */
    public static UserType getUserType() {
        return UserType.getUserType(getLoginUser().getUserType());
    }

    /**
     * 是否为超级管理员。
     */
    public static boolean isSuperAdmin(Long userId) {
        return SystemConstants.SUPER_ADMIN_ID.equals(userId);
    }

    /**
     * 是否为超级管理员。
     */
    public static boolean isSuperAdmin() {
        return isSuperAdmin(getUserId());
    }

    /**
     * 是否为租户管理员。
     */
    public static boolean isTenantAdmin(Set<String> rolePermission) {
        if (CollUtil.isEmpty(rolePermission)) {
            return false;
        }
        return rolePermission.contains(TenantConstants.TENANT_ADMIN_ROLE_KEY);
    }

    /**
     * 是否为租户管理员。
     */
    public static boolean isTenantAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && isTenantAdmin(loginUser.getRolePermission());
    }

    /**
     * 检查当前用户是否已登录。
     */
    public static boolean isLogin() {
        try {
            return getLoginUser() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 退出登录。
     */
    public static void logout() {
        try {
            SecurityUtils.getSubject().logout();
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取当前登录凭证（token，即 Shiro 会话 ID）。
     */
    public static String getToken() {
        return SecurityUtils.getSubject().getSession().getId().toString();
    }

    /**
     * 获取当前会话剩余有效期（秒）。
     */
    public static long getTokenTimeout() {
        long timeout = SecurityUtils.getSubject().getSession().getTimeout();
        return timeout > 0 ? timeout / 1000 : timeout;
    }

}

package com.zero.admin.system.service.impl;

import cn.hutool.core.util.ArrayUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.base.sensitive.core.SensitiveService;
import com.zero.admin.base.tenant.helper.TenantHelper;
import org.springframework.stereotype.Service;

/**
 * 脱敏服务
 * 默认管理员不过滤
 * 需自行根据业务重写实现
 *
 * @author Akai
 */
@Service
public class SysSensitiveServiceImpl implements SensitiveService {

    /**
     * 是否脱敏
     */
    @Override
    public boolean isSensitive(String[] roleKey, String[] perms) {
        if (!LoginHelper.isLogin()) {
            return true;
        }
        Subject subject = SecurityUtils.getSubject();
        boolean roleExist = ArrayUtil.isNotEmpty(roleKey);
        boolean permsExist = ArrayUtil.isNotEmpty(perms);
        if (roleExist && permsExist) {
            if (hasAnyRole(subject, roleKey) && hasAnyPermission(subject, perms)) {
                return false;
            }
        } else if (roleExist && hasAnyRole(subject, roleKey)) {
            return false;
        } else if (permsExist && hasAnyPermission(subject, perms)) {
            return false;
        }

        if (TenantHelper.isEnable()) {
            return !LoginHelper.isSuperAdmin() && !LoginHelper.isTenantAdmin();
        }
        return !LoginHelper.isSuperAdmin();
    }

    private static boolean hasAnyRole(Subject subject, String[] roleKeys) {
        for (String roleKey : roleKeys) {
            if (subject.hasRole(roleKey)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyPermission(Subject subject, String[] perms) {
        for (String perm : perms) {
            if (subject.isPermitted(perm)) {
                return true;
            }
        }
        return false;
    }

}

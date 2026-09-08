package com.zero.admin.member.domain.model;

import com.zero.admin.base.core.domain.model.LoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端 Shiro principal。其 userType 固定为 app_user，不加载后台角色和菜单权限。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberLoginUser extends LoginUser {
    private String loginSource;
}

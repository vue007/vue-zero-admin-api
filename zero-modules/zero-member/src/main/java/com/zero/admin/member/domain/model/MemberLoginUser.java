package com.zero.admin.member.domain.model;

import com.zero.admin.base.core.domain.model.LoginUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** C 端 Shiro principal。其 userType 固定为 app_user，不加载后台角色和菜单权限。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberLoginUser extends LoginUser {
    private Long applicationId;
    private String appId;
    /** 登录时的 scope 快照；实时授权仍由 C 端入口重新校验。 */
    private List<String> appScopes;
    private String loginSource;
}

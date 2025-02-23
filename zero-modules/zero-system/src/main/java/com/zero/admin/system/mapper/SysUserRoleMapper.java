package com.zero.admin.system.mapper;

import com.zero.admin.base.mybatis.core.mapper.BaseMapperPlus;
import com.zero.admin.system.domain.SysUserRole;

import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author Akai
 */
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRole, SysUserRole> {

    /**
     * 根据角色ID查询关联的用户ID列表
     *
     * @param roleId 角色ID
     * @return 关联到指定角色的用户ID列表
     */
    List<Long> selectUserIdsByRoleId(Long roleId);

}

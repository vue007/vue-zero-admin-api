package com.zero.admin.tenantapp.mapper;

import com.zero.admin.base.mybatis.core.mapper.BaseMapperPlus;
import com.zero.admin.tenantapp.domain.TenantApplication;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationScopeVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.tenantapp.domain.vo.TenantNameVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/** 租户 App 接入 Mapper。 */
public interface TenantApplicationMapper extends BaseMapperPlus<TenantApplication, TenantApplicationVo> {

    @Select("""
        select regexp_replace(perms, ':[^:]+$', '') as value,
               menu_name as label
        from sys_menu
        where parent_id = 7
          and menu_type = 'C'
          and visible = '0'
          and status = '0'
          and menu_id <> 127
          and perms is not null
          and perms <> ''
        order by order_num, menu_id
        """)
    List<TenantApplicationScopeVo> selectAppScopeOptions();

    @Select("select exists(select 1 from sys_tenant where tenant_id = #{tenantId} and del_flag = '0')")
    boolean tenantExists(@Param("tenantId") String tenantId);

    @Select("""
        select exists(
            select 1
            from sys_tenant
            where tenant_id = #{tenantId}
              and del_flag = '0'
              and status = '0'
              and (expire_time is null or expire_time > current_timestamp)
        )
        """)
    boolean tenantAvailable(@Param("tenantId") String tenantId);

    @Select({
        "<script>",
        "select tenant_id as tenantId, company_name as tenantName",
        "from sys_tenant",
        "where del_flag = '0' and tenant_id in",
        "<foreach collection='tenantIds' item='tenantId' open='(' separator=',' close=')'>",
        "#{tenantId}",
        "</foreach>",
        "</script>"
    })
    List<TenantNameVo> selectTenantNames(@Param("tenantIds") Collection<String> tenantIds);
}

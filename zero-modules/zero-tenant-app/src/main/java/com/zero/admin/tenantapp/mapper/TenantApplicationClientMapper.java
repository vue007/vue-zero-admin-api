package com.zero.admin.tenantapp.mapper;

import com.zero.admin.base.mybatis.core.mapper.BaseMapperPlus;
import com.zero.admin.tenantapp.domain.TenantApplicationClient;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientOptionVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/** App 终端认证策略绑定 Mapper。 */
public interface TenantApplicationClientMapper
    extends BaseMapperPlus<TenantApplicationClient, TenantApplicationClientVo> {

    @Select({
        "<script>",
        "select binding.id, binding.tenant_id as tenantId,",
        "       binding.application_id as applicationId,",
        "       binding.auth_client_id as authClientId,",
        "       binding.channel, binding.status,",
        "       client.client_key as clientKey,",
        "       client.device_type as deviceType,",
        "       client.grant_type as grantType,",
        "       client.timeout, client.active_timeout as activeTimeout,",
        "       client.status as clientStatus,",
        "       binding.create_time as createTime, binding.update_time as updateTime",
        "from app_application_client binding",
        "left join sys_client client",
        "  on client.id = binding.auth_client_id and client.del_flag = '0'",
        "where binding.del_flag = '0' and binding.application_id in",
        "<foreach collection='applicationIds' item='applicationId' open='(' separator=',' close=')'>",
        "#{applicationId}",
        "</foreach>",
        "order by binding.application_id, binding.channel, binding.id",
        "</script>"
    })
    List<TenantApplicationClientVo> selectByApplicationIds(
        @Param("applicationIds") Collection<Long> applicationIds);

    @Select("""
        select binding.id, binding.tenant_id as tenantId,
               binding.application_id as applicationId,
               binding.auth_client_id as authClientId,
               binding.channel, binding.status,
               client.client_id as clientId,
               client.client_key as clientKey,
               client.device_type as deviceType,
               client.grant_type as grantType,
               client.timeout, client.active_timeout as activeTimeout,
               client.status as clientStatus
        from app_application_client binding
        inner join sys_client client
          on client.id = binding.auth_client_id
         and client.del_flag = '0'
        where binding.application_id = #{applicationId}
          and binding.tenant_id = #{tenantId}
          and binding.channel = #{channel}
          and binding.del_flag = '0'
        limit 1
        """)
    TenantApplicationClientVo selectByApplicationAndChannel(
        @Param("applicationId") Long applicationId,
        @Param("tenantId") String tenantId,
        @Param("channel") String channel);

    @Select("""
        select id as authClientId, client_key as clientKey,
               device_type as deviceType, grant_type as grantType,
               timeout, active_timeout as activeTimeout, status as clientStatus
        from sys_client
        where del_flag = '0'
          and client_key <> 'pc'
        order by case when status = '0' then 0 else 1 end, client_key, id
        """)
    List<TenantApplicationClientOptionVo> selectClientOptions();

    @Select({
        "<script>",
        "select id as authClientId, client_key as clientKey,",
        "       device_type as deviceType, grant_type as grantType,",
        "       timeout, active_timeout as activeTimeout, status as clientStatus",
        "from sys_client",
        "where del_flag = '0' and client_key &lt;&gt; 'pc' and id in",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "order by id",
        "for update",
        "</script>"
    })
    List<TenantApplicationClientOptionVo> selectBindableClientsByIds(
        @Param("ids") Collection<Long> ids);
}

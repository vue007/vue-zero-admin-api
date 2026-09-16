package com.zero.admin.system.mapper;

import com.zero.admin.base.mybatis.core.mapper.BaseMapperPlus;
import com.zero.admin.system.domain.SysClient;
import com.zero.admin.system.domain.vo.SysClientVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 授权管理Mapper接口
 *
 * @author Akai
 */
public interface SysClientMapper extends BaseMapperPlus<SysClient, SysClientVo> {

    @Select({
        "<script>",
        "select id from sys_client where id in",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "order by id for update",
        "</script>"
    })
    List<Long> lockByIds(@Param("ids") Collection<Long> ids);

    @Select({
        "<script>",
        "select exists(",
        "  select 1 from app_application_client",
        "  where del_flag = '0' and auth_client_id in",
        "  <foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "    #{id}",
        "  </foreach>",
        ")",
        "</script>"
    })
    boolean hasApplicationBindings(@Param("ids") Collection<Long> ids);
}

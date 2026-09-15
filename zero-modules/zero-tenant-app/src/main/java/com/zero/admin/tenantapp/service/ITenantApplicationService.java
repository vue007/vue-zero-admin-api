package com.zero.admin.tenantapp.service;

import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationBo;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationStatusBo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationAuthVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationCredentialVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationScopeVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.tenantapp.domain.vo.TenantNameVo;

import java.util.Collection;
import java.util.List;

/** 租户 App 接入服务。 */
public interface ITenantApplicationService {

    TableDataInfo<TenantApplicationVo> queryCurrentTenantPage(TenantApplicationBo bo, PageQuery pageQuery);

    TableDataInfo<TenantApplicationVo> queryAllTenantPage(TenantApplicationBo bo, PageQuery pageQuery);

    TenantApplicationVo queryCurrentTenantById(Long id);

    TenantApplicationVo queryAllTenantById(Long id);

    /** 返回 App 管理下可选的业务模块授权范围。 */
    List<TenantApplicationScopeVo> queryScopeOptions();

    /** 仅供平台超级管理员选择应用所属租户。 */
    List<TenantNameVo> searchTenantOptions(String keyword);

    TenantApplicationCredentialVo createForCurrentTenant(TenantApplicationBo bo);

    TenantApplicationCredentialVo createForTenant(TenantApplicationBo bo);

    boolean updateForCurrentTenant(TenantApplicationBo bo);

    boolean updateForTenant(TenantApplicationBo bo);

    boolean changeStatusForCurrentTenant(TenantApplicationStatusBo bo);

    boolean changeStatusForTenant(TenantApplicationStatusBo bo);

    TenantApplicationCredentialVo resetSecretForCurrentTenant(Long id);

    TenantApplicationCredentialVo resetSecretForTenant(Long id);

    boolean deleteForCurrentTenant(Collection<Long> ids);

    boolean deleteForTenant(Collection<Long> ids);

    /** 公开 App ID 换取可信租户上下文，适用于不应内置长期 secret 的终端。 */
    TenantApplicationAuthVo resolveEnabledByAppId(String appId);

    /** 校验可信服务端持有的 App Secret，并返回可信租户上下文。 */
    TenantApplicationAuthVo authenticate(String appId, String appSecret);
}

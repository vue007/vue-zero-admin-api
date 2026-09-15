package com.zero.admin.tenantapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.MapstructUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.tenantapp.domain.TenantApplication;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationBo;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationStatusBo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationAuthVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationCredentialVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationScopeVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.tenantapp.domain.vo.TenantNameVo;
import com.zero.admin.tenantapp.mapper.TenantApplicationMapper;
import com.zero.admin.tenantapp.service.ITenantApplicationService;
import com.zero.admin.tenantapp.service.TenantApplicationSecretManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 租户 App 接入服务实现。 */
@Service
@RequiredArgsConstructor
public class TenantApplicationServiceImpl implements ITenantApplicationService {

    @Autowired(required = false)
    private final TenantApplicationMapper applicationMapper;
    private final TenantApplicationSecretManager secretManager;

    @Override
    public TableDataInfo<TenantApplicationVo> queryCurrentTenantPage(
        TenantApplicationBo bo, PageQuery pageQuery) {
        return queryPage(bo, pageQuery, false);
    }

    @Override
    public TableDataInfo<TenantApplicationVo> queryAllTenantPage(
        TenantApplicationBo bo, PageQuery pageQuery) {
        return TenantHelper.ignore(() -> queryPage(bo, pageQuery, true));
    }

    @Override
    public TenantApplicationVo queryCurrentTenantById(Long id) {
        return toVo(requireApplication(id));
    }

    @Override
    public TenantApplicationVo queryAllTenantById(Long id) {
        return TenantHelper.ignore(() -> toVo(requireApplication(id)));
    }

    @Override
    public List<TenantApplicationScopeVo> queryScopeOptions() {
        return applicationMapper.selectAppScopeOptions();
    }

    @Override
    public List<TenantNameVo> searchTenantOptions(String keyword) {
        String normalized = StringUtils.isBlank(keyword) ? "" : keyword.strip();
        return TenantHelper.ignore(() -> applicationMapper.searchTenantOptions(normalized));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantApplicationCredentialVo createForCurrentTenant(TenantApplicationBo bo) {
        return create(bo, requireCurrentTenantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantApplicationCredentialVo createForTenant(TenantApplicationBo bo) {
        String tenantId = normalizeTenantId(bo.getTenantId());
        return TenantHelper.ignore(() -> create(bo, tenantId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateForCurrentTenant(TenantApplicationBo bo) {
        return update(bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateForTenant(TenantApplicationBo bo) {
        return TenantHelper.ignore(() -> update(bo));
    }

    @Override
    public boolean changeStatusForCurrentTenant(TenantApplicationStatusBo bo) {
        return changeStatus(bo);
    }

    @Override
    public boolean changeStatusForTenant(TenantApplicationStatusBo bo) {
        return TenantHelper.ignore(() -> changeStatus(bo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantApplicationCredentialVo resetSecretForCurrentTenant(Long id) {
        return resetSecret(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantApplicationCredentialVo resetSecretForTenant(Long id) {
        return TenantHelper.ignore(() -> resetSecret(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteForCurrentTenant(Collection<Long> ids) {
        return delete(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteForTenant(Collection<Long> ids) {
        return TenantHelper.ignore(() -> delete(ids));
    }

    @Override
    public TenantApplicationAuthVo resolveEnabledByAppId(String appId) {
        return TenantHelper.ignore(() -> toAuthVo(requireEnabledApplication(appId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantApplicationAuthVo authenticate(String appId, String appSecret) {
        return TenantHelper.ignore(() -> {
            TenantApplication application = requireEnabledApplication(appId);
            if (!secretManager.matches(appSecret, application.getSecretHash())) {
                throw new ServiceException("应用凭证无效");
            }
            TenantApplication accessed = new TenantApplication();
            accessed.setId(application.getId());
            accessed.setLastAccessTime(new Date());
            if (applicationMapper.updateById(accessed) != 1) {
                throw new ServiceException("应用凭证无效");
            }
            return toAuthVo(application);
        });
    }

    private TableDataInfo<TenantApplicationVo> queryPage(
        TenantApplicationBo bo, PageQuery pageQuery, boolean includeTenantFilter) {
        Page<TenantApplicationVo> page = applicationMapper.selectVoPage(
            pageQuery.build(), buildQueryWrapper(bo, includeTenantFilter));
        hydrate(page.getRecords());
        return TableDataInfo.build(page);
    }

    private LambdaQueryWrapper<TenantApplication> buildQueryWrapper(
        TenantApplicationBo bo, boolean includeTenantFilter) {
        return Wrappers.<TenantApplication>lambdaQuery()
            .eq(includeTenantFilter && StringUtils.isNotBlank(bo.getTenantId()),
                TenantApplication::getTenantId, bo.getTenantId())
            .like(StringUtils.isNotBlank(bo.getAppName()),
                TenantApplication::getAppName, bo.getAppName())
            .eq(StringUtils.isNotBlank(bo.getAppId()),
                TenantApplication::getAppId, bo.getAppId())
            .eq(StringUtils.isNotBlank(bo.getStatus()),
                TenantApplication::getStatus, bo.getStatus())
            .orderByDesc(TenantApplication::getCreateTime)
            .orderByDesc(TenantApplication::getId);
    }

    private TenantApplicationCredentialVo create(TenantApplicationBo bo, String tenantId) {
        assertTenantExists(tenantId);
        normalize(bo);

        String rawSecret = secretManager.generateSecret();
        TenantApplication application = MapstructUtils.convert(bo, TenantApplication.class);
        if (application == null) {
            throw new ServiceException("应用信息转换失败");
        }
        clearAuditFields(application);
        application.setId(null);
        application.setTenantId(tenantId);
        application.setAppId(secretManager.generateAppId());
        application.setSecretHash(secretManager.hashSecret(rawSecret));
        application.setScopeCodes(serializeScopes(bo.getScopes()));
        application.setStatus(StringUtils.blankToDefault(bo.getStatus(), SystemConstants.NORMAL));
        application.setLastAccessTime(null);
        application.setSecretRotatedTime(new Date());
        application.setDelFlag(SystemConstants.NORMAL);

        if (applicationMapper.insert(application) != 1) {
            throw new ServiceException("创建应用失败");
        }
        return credential(application, rawSecret);
    }

    private boolean update(TenantApplicationBo bo) {
        TenantApplication existing = requireApplication(bo.getId());
        normalize(bo);

        TenantApplication update = MapstructUtils.convert(bo, TenantApplication.class);
        if (update == null) {
            throw new ServiceException("应用信息转换失败");
        }
        clearAuditFields(update);
        update.setTenantId(null);
        update.setAppId(null);
        update.setSecretHash(null);
        update.setScopeCodes(serializeScopes(bo.getScopes()));
        update.setStatus(null);
        update.setLastAccessTime(null);
        update.setSecretRotatedTime(null);
        update.setDelFlag(null);

        // 租户和 App ID 都是凭证身份的一部分，编辑操作不可转移或改写。
        update.setId(existing.getId());
        return applicationMapper.updateById(update) == 1;
    }

    private boolean changeStatus(TenantApplicationStatusBo bo) {
        TenantApplication existing = requireApplication(bo.getId());
        TenantApplication update = new TenantApplication();
        update.setId(existing.getId());
        update.setStatus(bo.getStatus());
        return applicationMapper.updateById(update) == 1;
    }

    private TenantApplicationCredentialVo resetSecret(Long id) {
        TenantApplication existing = requireApplication(id);
        String rawSecret = secretManager.generateSecret();

        TenantApplication update = new TenantApplication();
        update.setId(existing.getId());
        update.setSecretHash(secretManager.hashSecret(rawSecret));
        update.setSecretRotatedTime(new Date());
        if (applicationMapper.updateById(update) != 1) {
            throw new ServiceException("重置应用密钥失败");
        }
        existing.setSecretRotatedTime(update.getSecretRotatedTime());
        return credential(existing, rawSecret);
    }

    private boolean delete(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("应用ID不能为空");
        }
        // 先逐个读取，确保租户侧批量删除中不存在越权或失效 ID。
        ids.forEach(this::requireApplication);
        return applicationMapper.deleteByIds(ids) > 0;
    }

    private TenantApplication requireApplication(Long id) {
        if (id == null) {
            throw new ServiceException("应用ID不能为空");
        }
        TenantApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new ServiceException("应用不存在或无权访问");
        }
        return application;
    }

    private TenantApplication requireEnabledApplication(String appId) {
        if (StringUtils.isBlank(appId)) {
            throw new ServiceException("应用凭证无效");
        }
        TenantApplication application = applicationMapper.selectOne(
            Wrappers.<TenantApplication>lambdaQuery()
                .eq(TenantApplication::getAppId, appId.strip())
                .eq(TenantApplication::getStatus, SystemConstants.NORMAL));
        if (application == null || !applicationMapper.tenantAvailable(application.getTenantId())) {
            throw new ServiceException("应用凭证无效");
        }
        return application;
    }

    private void assertTenantExists(String tenantId) {
        if (!applicationMapper.tenantExists(tenantId)) {
            throw new ServiceException("所属租户不存在");
        }
    }

    private String requireCurrentTenantId() {
        return normalizeTenantId(TenantHelper.getTenantId());
    }

    private String normalizeTenantId(String tenantId) {
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("租户编号不能为空");
        }
        return tenantId.strip();
    }

    private void normalize(TenantApplicationBo bo) {
        bo.setAppName(bo.getAppName().strip());
        bo.setRemark(StringUtils.isBlank(bo.getRemark()) ? null : bo.getRemark().strip());
        bo.setScopes(normalizeScopes(bo.getScopes()));
        assertScopesAvailable(bo.getScopes());
    }

    private void assertScopesAvailable(List<String> scopes) {
        Set<String> allowedScopes = applicationMapper.selectAppScopeOptions().stream()
            .map(TenantApplicationScopeVo::getValue)
            .collect(Collectors.toSet());
        if (scopes == null || scopes.isEmpty() || !allowedScopes.containsAll(scopes)) {
            throw new ServiceException("授权范围包含不存在或已停用的 App 管理模块");
        }
    }

    private List<String> normalizeScopes(List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = scopes.stream()
            .filter(Objects::nonNull)
            .map(String::strip)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return List.copyOf(normalized);
    }

    private String serializeScopes(List<String> scopes) {
        return String.join(",", scopes == null ? List.of() : scopes);
    }

    private List<String> deserializeScopes(String scopeCodes) {
        if (StringUtils.isBlank(scopeCodes)) {
            return List.of();
        }
        return List.of(scopeCodes.split(","));
    }

    private TenantApplicationVo toVo(TenantApplication application) {
        TenantApplicationVo vo = MapstructUtils.convert(application, TenantApplicationVo.class);
        if (vo == null) {
            throw new ServiceException("应用信息转换失败");
        }
        hydrate(List.of(vo));
        return vo;
    }

    private void hydrate(List<TenantApplicationVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        records.forEach(vo -> vo.setScopes(deserializeScopes(vo.getScopeCodes())));

        List<String> tenantIds = records.stream()
            .map(TenantApplicationVo::getTenantId)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
        if (tenantIds.isEmpty()) {
            return;
        }
        Map<String, String> tenantNames = applicationMapper.selectTenantNames(tenantIds).stream()
            .collect(Collectors.toMap(TenantNameVo::getTenantId, TenantNameVo::getTenantName));
        records.forEach(vo -> vo.setTenantName(tenantNames.get(vo.getTenantId())));
    }

    private TenantApplicationAuthVo toAuthVo(TenantApplication application) {
        return new TenantApplicationAuthVo(
            application.getId(),
            application.getTenantId(),
            application.getAppId(),
            deserializeScopes(application.getScopeCodes())
        );
    }

    private TenantApplicationCredentialVo credential(
        TenantApplication application, String rawSecret) {
        return new TenantApplicationCredentialVo(
            application.getId(), application.getTenantId(), application.getAppId(), rawSecret);
    }

    private void clearAuditFields(TenantApplication application) {
        application.setCreateDept(null);
        application.setCreateBy(null);
        application.setCreateTime(null);
        application.setUpdateBy(null);
        application.setUpdateTime(null);
    }
}

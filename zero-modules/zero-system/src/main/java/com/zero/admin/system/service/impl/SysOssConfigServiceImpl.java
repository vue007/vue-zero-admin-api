package com.zero.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zero.admin.base.core.constant.CacheNames;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.MapstructUtils;
import com.zero.admin.base.core.utils.ObjectUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.base.mybatis.core.page.PageQuery;
import com.zero.admin.base.mybatis.core.page.TableDataInfo;
import com.zero.admin.base.oss.constant.OssConstant;
import com.zero.admin.base.redis.utils.CacheUtils;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.system.domain.SysOssConfig;
import com.zero.admin.system.domain.bo.SysOssConfigBo;
import com.zero.admin.system.domain.vo.SysOssConfigVo;
import com.zero.admin.system.mapper.SysOssConfigMapper;
import com.zero.admin.system.service.ISysOssConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 对象存储配置Service业务层处理
 *
 * @author Akai
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysOssConfigServiceImpl implements ISysOssConfigService {

    @Autowired(required = false) private final SysOssConfigMapper baseMapper;

    /**
     * 项目启动时，初始化参数到缓存，加载配置类
     */
    @Override
    public void init() {
        List<SysOssConfig> list = baseMapper.selectList();
        if (CollUtil.isEmpty(list)) {
            RedisUtils.deleteObject(OssConstant.DEFAULT_CONFIG_KEY);
            return;
        }
        SysOssConfig defaultConfig = list.stream()
            .filter(config -> "0".equals(config.getStatus()))
            .findFirst()
            .orElse(null);
        if (ObjectUtil.isNull(defaultConfig)) {
            defaultConfig = list.get(0);
            defaultConfig.setStatus("0");
            baseMapper.update(null, new LambdaUpdateWrapper<SysOssConfig>()
                .set(SysOssConfig::getStatus, "0")
                .eq(SysOssConfig::getOssConfigId, defaultConfig.getOssConfigId()));
            log.warn("未找到默认OSS配置，已自动启用配置: {}", defaultConfig.getConfigKey());
        }
        // 加载OSS初始化配置
        for (SysOssConfig config : list) {
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, defaultConfig.getConfigKey());
    }

    @Override
    public SysOssConfigVo queryById(Long ossConfigId) {
        return baseMapper.selectVoById(ossConfigId);
    }

    @Override
    public TableDataInfo<SysOssConfigVo> queryPageList(SysOssConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOssConfig> lqw = buildQueryWrapper(bo);
        Page<SysOssConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }


    private LambdaQueryWrapper<SysOssConfig> buildQueryWrapper(SysOssConfigBo bo) {
        LambdaQueryWrapper<SysOssConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getConfigKey()), SysOssConfig::getConfigKey, bo.getConfigKey());
        lqw.like(StringUtils.isNotBlank(bo.getBucketName()), SysOssConfig::getBucketName, bo.getBucketName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysOssConfig::getStatus, bo.getStatus());
        lqw.orderByAsc(SysOssConfig::getOssConfigId);
        return lqw;
    }

    @Override
    public Boolean insertByBo(SysOssConfigBo bo) {
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        prepareConfig(config);
        validEntityBeforeSave(config);
        boolean hasDefault = baseMapper.exists(new LambdaQueryWrapper<SysOssConfig>()
            .eq(SysOssConfig::getStatus, "0"));
        if (!hasDefault) {
            config.setStatus("0");
        }
        boolean flag = baseMapper.insert(config) > 0;
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
            if ("0".equals(config.getStatus())) {
                RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, config.getConfigKey());
            }
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SysOssConfigBo bo) {
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        SysOssConfig existing = baseMapper.selectById(config.getOssConfigId());
        if (ObjectUtil.isNull(existing)) {
            throw new ServiceException("对象存储配置不存在!");
        }
        if (StringUtils.isBlank(config.getAccessKey())) {
            config.setAccessKey(existing.getAccessKey());
        }
        if (StringUtils.isBlank(config.getSecretKey())) {
            config.setSecretKey(existing.getSecretKey());
        }
        prepareConfig(config);
        validEntityBeforeSave(config);
        LambdaUpdateWrapper<SysOssConfig> luw = new LambdaUpdateWrapper<>();
        luw.set(ObjectUtil.isNull(config.getPrefix()), SysOssConfig::getPrefix, "");
        luw.set(ObjectUtil.isNull(config.getRegion()), SysOssConfig::getRegion, "");
        luw.set(ObjectUtil.isNull(config.getExt1()), SysOssConfig::getExt1, "");
        luw.set(ObjectUtil.isNull(config.getRemark()), SysOssConfig::getRemark, "");
        luw.eq(SysOssConfig::getOssConfigId, config.getOssConfigId());
        boolean flag = baseMapper.update(config, luw) > 0;
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        return flag;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysOssConfig entity) {
        if (StringUtils.isNotEmpty(entity.getConfigKey())
            && !checkConfigKeyUnique(entity)) {
            throw new ServiceException("操作配置'" + entity.getConfigKey() + "'失败, 配置key已存在!");
        }
        if (isTencentCos(entity)) {
            if (StringUtils.isBlank(entity.getAccessKey()) || !entity.getAccessKey().startsWith("AKID")) {
                throw new ServiceException("腾讯云 COS Access Key 必须填写 API 密钥 SecretId（通常以 AKID 开头），不能填写 APPID");
            }
            if (StringUtils.isBlank(entity.getRegion())) {
                throw new ServiceException("腾讯云 COS 地域不能为空，例如 ap-guangzhou");
            }
            if (!entity.getBucketName().matches(".+-\\d+$")) {
                throw new ServiceException("腾讯云 COS 存储桶名称必须包含 APPID，例如 example-1250000000");
            }
        }
    }

    /**
     * OSS 客户端统一负责拼接协议，数据库只保存主机名，避免出现重复协议或尾部斜杠。
     */
    private void prepareConfig(SysOssConfig entity) {
        entity.setConfigKey(trim(entity.getConfigKey()));
        entity.setAccessKey(trim(entity.getAccessKey()));
        entity.setSecretKey(trim(entity.getSecretKey()));
        entity.setBucketName(trim(entity.getBucketName()));
        entity.setEndpoint(normalizeHost(entity.getEndpoint()));
        entity.setDomain(normalizeHost(entity.getDomain()));
        entity.setRegion(trim(entity.getRegion()));
        entity.setPrefix(trimSlashes(entity.getPrefix()));
        if (isTencentCos(entity) && StringUtils.isBlank(entity.getIsHttps())) {
            entity.setIsHttps("Y");
        }
    }

    private boolean isTencentCos(SysOssConfig entity) {
        return "qcloud".equalsIgnoreCase(entity.getConfigKey())
            || StringUtils.contains(entity.getEndpoint(), ".myqcloud.com");
    }

    private String normalizeHost(String value) {
        String result = trim(value);
        if (StringUtils.isBlank(result)) {
            return result;
        }
        result = result.replaceFirst("(?i)^https?://", "");
        return result.replaceFirst("/+$", "");
    }

    private String trimSlashes(String value) {
        String result = trim(value);
        return StringUtils.isBlank(result) ? result : result.replaceAll("^/+|/+$", "");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            if (CollUtil.containsAny(ids, OssConstant.SYSTEM_DATA_IDS)) {
                throw new ServiceException("系统内置, 不可删除!");
            }
        }
        List<SysOssConfig> list = CollUtil.newArrayList();
        for (Long configId : ids) {
            SysOssConfig config = baseMapper.selectById(configId);
            list.add(config);
        }
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        if (flag) {
            list.forEach(sysOssConfig ->
                CacheUtils.evict(CacheNames.SYS_OSS_CONFIG, sysOssConfig.getConfigKey()));
        }
        return flag;
    }

    /**
     * 判断configKey是否唯一
     */
    private boolean checkConfigKeyUnique(SysOssConfig sysOssConfig) {
        long ossConfigId = ObjectUtils.notNull(sysOssConfig.getOssConfigId(), -1L);
        SysOssConfig info = baseMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
            .select(SysOssConfig::getOssConfigId, SysOssConfig::getConfigKey)
            .eq(SysOssConfig::getConfigKey, sysOssConfig.getConfigKey()));
        if (ObjectUtil.isNotNull(info) && info.getOssConfigId() != ossConfigId) {
            return false;
        }
        return true;
    }

    /**
     * 启用禁用状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOssConfigStatus(SysOssConfigBo bo) {
        SysOssConfig sysOssConfig = baseMapper.selectById(bo.getOssConfigId());
        if (ObjectUtil.isNull(sysOssConfig)) {
            throw new ServiceException("对象存储配置不存在!");
        }
        sysOssConfig.setStatus("0");
        int row = baseMapper.update(null, new LambdaUpdateWrapper<SysOssConfig>()
            .set(SysOssConfig::getStatus, "1"));
        row += baseMapper.updateById(sysOssConfig);
        if (row > 0) {
            RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, sysOssConfig.getConfigKey());
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, sysOssConfig.getConfigKey(), JsonUtils.toJsonString(sysOssConfig));
        }
        return row;
    }

}

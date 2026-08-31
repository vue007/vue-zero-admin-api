package com.zero.admin.system.controller;

import com.zero.admin.base.core.constant.CacheNames;
import com.zero.admin.base.core.constant.TenantConstants;
import com.zero.admin.base.core.domain.R;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.idempotent.annotation.RepeatSubmit;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.base.log.enums.BusinessType;
import com.zero.admin.base.redis.utils.CacheUtils;
import com.zero.admin.base.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.redisson.api.RMap;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Redis 缓存监控与业务缓存管理。
 * <p>
 * 清理接口仅允许操作白名单中的 Spring Cache 缓存组，不提供任意 Redis key 删除能力。
 *
 * @author Akai
 */
@RequiredArgsConstructor
@RestController
@RequiresRoles(TenantConstants.SUPER_ADMIN_ROLE_KEY)
@RequestMapping("/monitor/cache")
public class CacheController {

    private static final Map<String, CacheDefinition> CACHE_DEFINITIONS = cacheDefinitions();

    private final RedissonConnectionFactory connectionFactory;

    /**
     * Redis 运行状态与命令统计。
     */
    @RequiresPermissions("monitor:cache:list")
    @GetMapping
    public R<CacheInfoVo> getInfo() {
        RedisConnection connection = connectionFactory.getConnection();
        try {
            Properties info = connection.commands().info();
            Properties commandStats = connection.commands().info("commandstats");
            List<CommandStatVo> stats = new ArrayList<>();
            if (commandStats != null) {
                commandStats.stringPropertyNames().forEach(key -> {
                    String property = commandStats.getProperty(key);
                    String calls = StringUtils.substringBetween(property, "calls=", ",usec");
                    stats.add(new CommandStatVo(
                        StringUtils.removeStart(key, "cmdstat_"), parseLong(calls)));
                });
                stats.sort(Comparator.comparingLong(CommandStatVo::value).reversed());
            }
            return R.ok(new CacheInfoVo(info, connection.commands().dbSize(), stats));
        } finally {
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
    }

    /**
     * 获取允许管理的业务缓存组。
     */
    @RequiresPermissions("monitor:cache:list")
    @GetMapping("/groups")
    public R<List<CacheGroupVo>> getCacheGroups() {
        List<CacheGroupVo> groups = CACHE_DEFINITIONS.entrySet().stream()
            .map(entry -> new CacheGroupVo(entry.getKey(), entry.getValue().remark(),
                RedisUtils.getClient().getMap(entry.getKey()).size()))
            .toList();
        return R.ok(groups);
    }

    /**
     * 获取缓存组内的键名，不返回缓存值，避免敏感业务数据泄露。
     */
    @RequiresPermissions("monitor:cache:list")
    @GetMapping("/groups/{cacheName}/keys")
    public R<List<String>> getCacheKeys(@PathVariable String cacheName) {
        CacheDefinition definition = getDefinition(cacheName);
        RMap<Object, Object> cache = RedisUtils.getClient().getMap(definition.cacheName());
        List<String> keys = cache.keySet().stream()
            .map(String::valueOf)
            .sorted()
            .toList();
        return R.ok(keys);
    }

    /**
     * 清空指定业务缓存组。
     */
    @RequiresPermissions("monitor:cache:clear")
    @Log(title = "缓存管理", businessType = BusinessType.CLEAN)
    @RepeatSubmit
    @DeleteMapping("/groups/{cacheName}")
    public R<Void> clearCacheGroup(@PathVariable String cacheName) {
        CacheDefinition definition = getDefinition(cacheName);
        CacheUtils.clear(definition.configuredName());
        return R.ok();
    }

    /**
     * 删除缓存组中的单个键。
     */
    @RequiresPermissions("monitor:cache:clear")
    @Log(title = "缓存管理", businessType = BusinessType.DELETE)
    @RepeatSubmit
    @DeleteMapping("/groups/{cacheName}/keys")
    public R<Void> clearCacheKey(@PathVariable String cacheName,
                                 @RequestParam String cacheKey) {
        CacheDefinition definition = getDefinition(cacheName);
        RMap<Object, Object> cache = RedisUtils.getClient().getMap(definition.cacheName());
        cache.keySet().stream()
            .filter(key -> String.valueOf(key).equals(cacheKey))
            .findFirst()
            .ifPresent(key -> CacheUtils.evict(definition.configuredName(), key));
        return R.ok();
    }

    private CacheDefinition getDefinition(String cacheName) {
        CacheDefinition definition = CACHE_DEFINITIONS.get(cacheName);
        if (definition == null) {
            throw new ServiceException("不允许管理该缓存组");
        }
        return definition;
    }

    private static Map<String, CacheDefinition> cacheDefinitions() {
        Map<String, CacheDefinition> definitions = new LinkedHashMap<>();
        add(definitions, CacheNames.SYS_CONFIG, "系统参数");
        add(definitions, CacheNames.SYS_DICT, "数据字典");
        add(definitions, CacheNames.SYS_TENANT, "租户信息");
        add(definitions, CacheNames.SYS_CLIENT, "客户端配置");
        add(definitions, CacheNames.SYS_USER_NAME, "用户账号");
        add(definitions, CacheNames.SYS_NICKNAME, "用户昵称");
        add(definitions, CacheNames.SYS_DEPT, "部门信息");
        add(definitions, CacheNames.SYS_OSS, "文件信息");
        add(definitions, CacheNames.SYS_ROLE_CUSTOM, "角色数据范围");
        add(definitions, CacheNames.SYS_DEPT_AND_CHILD, "部门层级");
        return definitions;
    }

    private static void add(Map<String, CacheDefinition> definitions,
                            String configuredName, String remark) {
        String cacheName = StringUtils.substringBefore(configuredName, "#");
        definitions.put(cacheName, new CacheDefinition(cacheName, configuredName, remark));
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private record CacheDefinition(String cacheName, String configuredName, String remark) {
    }

    public record CacheInfoVo(Properties info, Long dbSize, List<CommandStatVo> commandStats) {
    }

    public record CommandStatVo(String name, long value) {
    }

    public record CacheGroupVo(String cacheName, String remark, int keyCount) {
    }
}

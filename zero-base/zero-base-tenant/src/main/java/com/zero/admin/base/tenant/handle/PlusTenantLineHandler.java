package com.zero.admin.base.tenant.handle;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.base.tenant.properties.TenantProperties;

import java.util.List;

/**
 * 自定义租户处理器
 *
 * @author Akai
 */
@Slf4j
public class PlusTenantLineHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    public PlusTenantLineHandler(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    /**
     * 获取当前租户 ID。保留为受保护方法，便于在不启动 Spring、Shiro 和 Redis 的情况下
     * 验证租户 SQL 注入的失效策略。
     */
    protected String getCurrentTenantId() {
        return TenantHelper.getTenantId();
    }

    @Override
    public Expression getTenantId() {
        String tenantId = getCurrentTenantId();
        if (StringUtils.isBlank(tenantId)) {
            log.error("无法获取有效的租户id -> Null");
            return new NullValue();
        }
        // 返回固定租户
        return new StringValue(tenantId);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 不需要过滤租户的表
        List<String> excludes = tenantProperties.getExcludes();
        // 非业务表
        List<String> tables = ListUtil.toList(
            "gen_table",
            "gen_table_column"
        );
        if (excludes != null) {
            tables.addAll(excludes);
        }
        if (tables.contains(tableName)) {
            return true;
        }

        // 缺失租户上下文时仍然处理业务表，随后 getTenantId() 返回 NULL，
        // 生成 tenant_id = NULL 的失效关闭条件，避免无租户条件查询全部数据。
        return false;
    }

}

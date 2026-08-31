package com.zero.admin.test;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.zero.admin.base.mybatis.helper.DataPermissionHelper;
import com.zero.admin.base.tenant.handle.PlusTenantLineHandler;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.base.tenant.properties.TenantProperties;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class TenantIsolationUnitTest {

    @AfterEach
    void clearInterceptorContext() {
        InterceptorIgnoreHelper.clearIgnoreStrategy();
    }

    @Test
    void businessTablesRemainTenantFilteredWhenTenantContextIsMissing() {
        TestTenantLineHandler handler = handlerWithTenant(null);

        assertFalse(handler.ignoreTable("sys_user"));
        assertInstanceOf(NullValue.class, handler.getTenantId());
    }

    @Test
    void configuredAndInfrastructureTablesRemainExcluded() {
        TestTenantLineHandler handler = handlerWithTenant(null);

        assertTrue(handler.ignoreTable("sys_menu"));
        assertTrue(handler.ignoreTable("gen_table"));
        assertTrue(handler.ignoreTable("gen_table_column"));
    }

    @Test
    void presentTenantIsInjectedAsAStringLiteral() {
        TestTenantLineHandler handler = handlerWithTenant("000001");

        StringValue tenant = assertInstanceOf(StringValue.class, handler.getTenantId());
        assertEquals("000001", tenant.getValue());
        assertFalse(handler.ignoreTable("sys_user"));
    }

    @Test
    void nestedTenantIgnoreIsRestoredAfterNormalAndExceptionalExecution() {
        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));

        TenantHelper.ignore(() -> {
            assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
            TenantHelper.ignore(() -> assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test")));
            assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
        });
        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));

        assertThrows(IllegalStateException.class,
            () -> TenantHelper.ignore(() -> { throw new IllegalStateException("expected"); }));
        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
    }

    @Test
    void tenantAndDataPermissionIgnoreScopesDoNotClearEachOther() {
        TenantHelper.ignore(() -> {
            assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
            assertFalse(InterceptorIgnoreHelper.willIgnoreDataPermission("test"));

            DataPermissionHelper.ignore(() -> {
                assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
                assertTrue(InterceptorIgnoreHelper.willIgnoreDataPermission("test"));
            });

            assertTrue(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
            assertFalse(InterceptorIgnoreHelper.willIgnoreDataPermission("test"));
        });

        assertFalse(InterceptorIgnoreHelper.willIgnoreTenantLine("test"));
        assertFalse(InterceptorIgnoreHelper.willIgnoreDataPermission("test"));
    }

    private static TestTenantLineHandler handlerWithTenant(String tenantId) {
        TenantProperties properties = new TenantProperties();
        properties.setEnable(true);
        properties.setExcludes(List.of("sys_menu", "sys_tenant"));
        return new TestTenantLineHandler(properties, tenantId);
    }

    private static final class TestTenantLineHandler extends PlusTenantLineHandler {

        private final String tenantId;

        private TestTenantLineHandler(TenantProperties tenantProperties, String tenantId) {
            super(tenantProperties);
            this.tenantId = tenantId;
        }

        @Override
        protected String getCurrentTenantId() {
            return tenantId;
        }
    }
}

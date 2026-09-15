package com.zero.admin.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zero.admin.base.core.constant.TenantConstants;
import com.zero.admin.base.log.annotation.Log;
import com.zero.admin.tenantapp.domain.TenantApplication;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationBo;
import com.zero.admin.tenantapp.domain.bo.TenantApplicationStatusBo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationAuthVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationVo;
import com.zero.admin.web.controller.app.ApplicationController;
import com.zero.admin.web.controller.tenant.SysTenantAppController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("dev")
class TenantApplicationContractUnitTest {

    @Test
    void platformAndTenantControllersKeepSeparateRoutesAndPermissions() throws Exception {
        assertArrayEquals(
            new String[]{"/system/tenant-app"},
            SysTenantAppController.class.getAnnotation(RequestMapping.class).value()
        );
        assertArrayEquals(
            new String[]{TenantConstants.SUPER_ADMIN_ROLE_KEY},
            SysTenantAppController.class.getAnnotation(RequiresRoles.class).value()
        );
        assertArrayEquals(
            new String[]{"/app/application"},
            ApplicationController.class.getAnnotation(RequestMapping.class).value()
        );

        assertPermission(
            SysTenantAppController.class.getMethod(
                "changeStatus", TenantApplicationStatusBo.class),
            "system:tenantApp:status"
        );
        assertPermission(
            ApplicationController.class.getMethod(
                "changeStatus", TenantApplicationStatusBo.class),
            "app:application:status"
        );
        assertPermission(
            SysTenantAppController.class.getMethod("scopeOptions"),
            "system:tenantApp:list"
        );
        assertPermission(
            ApplicationController.class.getMethod("scopeOptions"),
            "app:application:list"
        );
        assertPermission(
            SysTenantAppController.class.getMethod("resetSecret", Long.class),
            "system:tenantApp:resetSecret"
        );
        assertPermission(
            ApplicationController.class.getMethod("resetSecret", Long.class),
            "app:application:resetSecret"
        );
    }

    @Test
    void oneTimeSecretResponsesAreExcludedFromOperationLogs() throws Exception {
        assertResponseLoggingDisabled(
            SysTenantAppController.class.getMethod("add", TenantApplicationBo.class)
        );
        assertResponseLoggingDisabled(
            SysTenantAppController.class.getMethod("resetSecret", Long.class)
        );
        assertResponseLoggingDisabled(
            ApplicationController.class.getMethod("add", TenantApplicationBo.class)
        );
        assertResponseLoggingDisabled(
            ApplicationController.class.getMethod("resetSecret", Long.class)
        );
    }

    @Test
    void persistentSecretHashCannotLeakThroughManagementView() throws Exception {
        assertNotNull(
            TenantApplication.class.getDeclaredField("secretHash")
                .getAnnotation(JsonIgnore.class)
        );
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplicationVo.class.getDeclaredField("secretHash"));
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplicationVo.class.getDeclaredField("appSecret"));
        assertNotNull(
            TenantApplicationVo.class.getDeclaredField("scopeCodes")
                .getAnnotation(JsonIgnore.class)
        );
    }

    @Test
    void clientChannelTypeIsNotDuplicatedInApplicationCredential() {
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplication.class.getDeclaredField("appType"));
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplicationBo.class.getDeclaredField("appType"));
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplicationVo.class.getDeclaredField("appType"));
        assertThrows(NoSuchFieldException.class,
            () -> TenantApplicationAuthVo.class.getDeclaredField("appType"));
    }

    private static void assertPermission(Method method, String expected) {
        RequiresPermissions permission = method.getAnnotation(RequiresPermissions.class);
        assertNotNull(permission);
        assertArrayEquals(new String[]{expected}, permission.value());
    }

    private static void assertResponseLoggingDisabled(Method method) {
        Log log = method.getAnnotation(Log.class);
        assertNotNull(log);
        assertFalse(log.isSaveResponseData());
    }
}

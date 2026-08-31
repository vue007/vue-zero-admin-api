package com.zero.admin.test;

import com.zero.admin.system.controller.SysMenuController;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("dev")
class MenuAuthorizationContractUnitTest {

    @Test
    void roleMenuTreeUsesRolePermissionInsteadOfMenuManagementPermission() throws Exception {
        Method endpoint = SysMenuController.class.getMethod("roleMenuTreeselect", Long.class);

        RequiresPermissions permission = endpoint.getAnnotation(RequiresPermissions.class);

        assertArrayEquals(new String[]{"system:role:list"}, permission.value());
    }

    @Test
    void sharedMenuTreeSupportsRoleManagementWithoutGrantingMenuManagement() throws Exception {
        Method endpoint = SysMenuController.class.getMethod(
            "treeselect",
            com.zero.admin.system.domain.bo.SysMenuBo.class
        );

        RequiresPermissions permission = endpoint.getAnnotation(RequiresPermissions.class);

        assertArrayEquals(
            new String[]{"system:menu:query", "system:role:list"},
            permission.value()
        );
        assertEquals(Logical.OR, permission.logical());
    }
}

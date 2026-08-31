package com.zero.admin.test;

import com.zero.admin.base.mybatis.annotation.DataColumn;
import com.zero.admin.base.mybatis.annotation.DataPermission;
import com.zero.admin.system.mapper.SysDeptMapper;
import com.zero.admin.system.mapper.SysPostMapper;
import com.zero.admin.system.mapper.SysRoleMapper;
import com.zero.admin.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class DataPermissionContractUnitTest {

    private static final List<Class<?>> DATA_SCOPED_MAPPERS = List.of(
        SysUserMapper.class,
        SysDeptMapper.class,
        SysPostMapper.class,
        SysRoleMapper.class
    );

    @Test
    void everyDataPermissionDeclarationHasAValidColumnMapping() {
        for (Class<?> mapper : DATA_SCOPED_MAPPERS) {
            for (Method method : mapper.getDeclaredMethods()) {
                DataPermission permission = method.getAnnotation(DataPermission.class);
                if (permission == null) {
                    continue;
                }

                assertTrue(permission.value().length > 0, method + " must declare at least one data column");
                assertTrue(permission.joinStr().isBlank()
                        || permission.joinStr().equalsIgnoreCase("AND")
                        || permission.joinStr().equalsIgnoreCase("OR"),
                    method + " has an unsupported joinStr");

                for (DataColumn column : permission.value()) {
                    assertEquals(column.key().length, column.value().length,
                        method + " has mismatched data permission keys and columns");
                    assertTrue(Arrays.stream(column.key()).allMatch(value -> value != null && !value.isBlank()),
                        method + " has a blank data permission key");
                    assertTrue(Arrays.stream(column.value()).allMatch(value -> value != null && !value.isBlank()),
                        method + " has a blank data permission column");
                }
            }
        }
    }

    @Test
    void criticalUserReadAndWriteMethodsCannotLoseDataPermissionAnnotations() {
        Set<String> protectedMethods = Arrays.stream(SysUserMapper.class.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(DataPermission.class))
            .map(Method::getName)
            .collect(Collectors.toSet());

        assertTrue(protectedMethods.containsAll(Set.of(
            "selectPageUserList",
            "selectUserList",
            "selectUserExportList",
            "selectAllocatedList",
            "selectUnallocatedList",
            "countUserById",
            "update",
            "updateById"
        )));
    }

    @Test
    void selfScopeUsesTheExpectedUserColumnsOnCriticalMappers() {
        assertDataColumn(SysUserMapper.class, "selectPageUserList", "userName", "u.user_id");
        assertDataColumn(SysUserMapper.class, "selectUserList", "userName", "user_id");
        assertDataColumn(SysRoleMapper.class, "selectRoleById", "userName", "r.create_by");
        assertDataColumn(SysPostMapper.class, "selectPagePostList", "userName", "create_by");
    }

    private static void assertDataColumn(Class<?> mapper, String methodName, String key, String value) {
        Method method = Arrays.stream(mapper.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(methodName))
            .findFirst()
            .orElseThrow();
        DataPermission permission = method.getAnnotation(DataPermission.class);
        assertNotNull(permission, method + " must have @DataPermission");

        boolean matches = Arrays.stream(permission.value())
            .anyMatch(column -> containsPair(column, key, value));
        assertTrue(matches, method + " must map " + key + " to " + value);
    }

    private static boolean containsPair(DataColumn column, String key, String value) {
        for (int index = 0; index < column.key().length; index++) {
            if (column.key()[index].equals(key) && column.value()[index].equals(value)) {
                return true;
            }
        }
        return false;
    }
}

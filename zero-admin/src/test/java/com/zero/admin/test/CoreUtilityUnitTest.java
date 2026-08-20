package com.zero.admin.test;

import com.zero.admin.base.core.enums.BusinessStatusEnum;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.ObjectUtils;
import com.zero.admin.base.core.utils.StreamUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreUtilityUnitTest {

    @Test
    void userTypeLookupSupportsExactSubstringAndUnknownValues() {
        assertEquals(UserType.SYS_USER, UserType.getUserType("sys_user"));
        assertEquals(UserType.APP_USER, UserType.getUserType("tenant-app_user"));
        assertThrows(RuntimeException.class, () -> UserType.getUserType("unknown"));
    }

    @Test
    void businessStatusProvidesLookupsListsAndPredicates() {
        assertEquals(BusinessStatusEnum.FINISH, BusinessStatusEnum.getByStatus("finish"));
        assertNull(BusinessStatusEnum.getByStatus("unknown"));
        assertEquals("已完成", BusinessStatusEnum.findByStatus("finish"));
        assertEquals("", BusinessStatusEnum.findByStatus(" "));
        assertEquals("", BusinessStatusEnum.findByStatus("unknown"));
        assertEquals(List.of("draft", "waiting", "back", "cancel"), BusinessStatusEnum.runningStatus());
        assertEquals(List.of("finish", "invalid", "termination"), BusinessStatusEnum.finishStatus());
        assertTrue(BusinessStatusEnum.isDraftOrCancelOrBack("back"));
        assertFalse(BusinessStatusEnum.isDraftOrCancelOrBack("waiting"));
        assertTrue(BusinessStatusEnum.initialState("termination"));
        assertFalse(BusinessStatusEnum.initialState("draft"));
    }

    @Test
    void businessStatusValidationRejectsInvalidTransitions() {
        ServiceException waiting = assertThrows(ServiceException.class,
            () -> BusinessStatusEnum.checkStartStatus("waiting"));
        assertEquals("该单据已提交过申请,正在审批中！", waiting.getMessage());

        ServiceException blank = assertThrows(ServiceException.class,
            () -> BusinessStatusEnum.checkCancelStatus(""));
        assertEquals("流程状态为空！", blank.getMessage());

        assertThrows(ServiceException.class, () -> BusinessStatusEnum.checkBackStatus("cancel"));
        assertThrows(ServiceException.class, () -> BusinessStatusEnum.checkInvalidStatus("finish"));
        BusinessStatusEnum.checkInvalidStatus("draft");
    }

    @Test
    void objectUtilsHandlesNullsDefaultsAndFunctions() {
        assertEquals("value", ObjectUtils.notNullGetter("value", (String value) -> value.toUpperCase()).toLowerCase());
        assertNull(ObjectUtils.notNullGetter(null, (String value) -> value.toUpperCase()));
        assertNull(ObjectUtils.notNullGetter("value", null));
        assertEquals("fallback", ObjectUtils.notNullGetter(null, (String value) -> value.toUpperCase(), "fallback"));
        assertEquals("value", ObjectUtils.notNull("value", "fallback"));
        assertEquals("fallback", ObjectUtils.notNull(null, "fallback"));
    }

    @Test
    void streamUtilsCoversEmptyAndCollectionTransformations() {
        List<Integer> values = Arrays.asList(3, null, 1, 2, 2);
        assertEquals(List.of(2, 2), StreamUtils.filter(values, value -> value != null && value == 2));
        assertEquals(1, StreamUtils.findFirst(values, value -> value != null && value < 2));
        assertEquals(2, StreamUtils.findAny(values, value -> value != null && value == 2).orElseThrow());
        assertEquals("3|null|1|2|2", StreamUtils.join(values, String::valueOf, "|"));
        assertEquals(List.of(1, 2, 2, 3), StreamUtils.sorted(values, (left, right) -> Integer.compare(left, right)));
        assertEquals(List.of(), StreamUtils.filter(Collections.emptyList(), value -> true));
        assertEquals("", StreamUtils.join(Collections.emptyList(), String::valueOf));
    }

    @Test
    void streamUtilsBuildsMapsGroupsAndMergesDuplicates() {
        List<String> values = Arrays.asList("a1", "a2", "b1", null);
        assertEquals(Map.of("a", "a1", "b", "b1"), StreamUtils.toIdentityMap(values, value -> value.substring(0, 1)));
        assertEquals(Map.of("a", 2, "b", 2), StreamUtils.toMap(values, value -> value.substring(0, 1), value -> value.length()));
        assertEquals(Map.of("a", List.of("a1", "a2"), "b", List.of("b1")),
            StreamUtils.groupByKey(values, value -> value.substring(0, 1)));
        assertEquals(Map.of("a", Map.of(1, List.of("a1"), 2, List.of("a2")), "b", Map.of(1, List.of("b1"))),
            StreamUtils.groupBy2Key(values, value -> value.substring(0, 1), value -> Integer.parseInt(value.substring(1))));
        assertEquals(Map.of("a", Map.of(1, "a1", 2, "a2"), "b", Map.of(1, "b1")),
            StreamUtils.group2Map(values, value -> value.substring(0, 1), value -> Integer.parseInt(value.substring(1))));
        assertEquals(Map.of("a", 1, "b", 2),
            StreamUtils.merge(Map.of("a", 1), Map.of("b", 2), (left, right) -> left == null ? right : right == null ? left : left));
        assertEquals(List.of("A1", "B1"), StreamUtils.toList(List.of("a1", "b1"), value -> value.toUpperCase()));
        assertEquals(List.of(), StreamUtils.toSet(List.of("a", "b"), value -> null).stream().toList());
    }
}

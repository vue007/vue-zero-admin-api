package com.zero.admin.test;

import com.zero.admin.base.core.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 带参数单元测试案例
 *
 * @author Akai
 */
@DisplayName("带参数单元测试案例")
public class ParamUnitTest {

    @DisplayName("测试 @ValueSource 注解")
    @ParameterizedTest
    @ValueSource(strings = {"t1", "t2", "t3"})
    void testValueSource(String str) {
        System.out.println(str);
    }

    @DisplayName("测试 @NullSource 注解")
    @ParameterizedTest
    @NullSource
    void testNullSource(String str) {
        System.out.println(str);
    }

    @DisplayName("测试 @EnumSource 注解")
    @ParameterizedTest
    @EnumSource(UserType.class)
    void testEnumSource(UserType type) {
        System.out.println(type.getUserType());
    }

    @DisplayName("测试 @MethodSource 注解")
    @ParameterizedTest
    @MethodSource("getParam")
    void testMethodSource(String str) {
        System.out.println(str);
    }

    static Stream<String> getParam() {
        List<String> list = new ArrayList<>();
        list.add("t1");
        list.add("t2");
        list.add("t3");
        return list.stream();
    }

    @BeforeEach
    void testBeforeEach() {
        System.out.println("@BeforeEach ==================");
    }

    @AfterEach
    void testAfterEach() {
        System.out.println("@AfterEach ==================");
    }


}

package com.zero.admin.test;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 标签单元测试案例
 *
 * @author Akai
 */
@SpringBootTest
@DisplayName("标签单元测试案例")
public class TagUnitTest {

    @Tag("dev")
    @DisplayName("测试 @Tag dev")
    @Test
    void testTagDev() {
        System.out.println("dev");
    }

    @Tag("prod")
    @DisplayName("测试 @Tag prod")
    @Test
    void testTagProd() {
        System.out.println("prod");
    }

    @Tag("local")
    @DisplayName("测试 @Tag local")
    @Test
    void testTagLocal() {
        System.out.println("local");
    }

    @Tag("exclude")
    @DisplayName("测试 @Tag exclude")
    @Test
    void testTagExclude() {
        System.out.println("exclude");
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

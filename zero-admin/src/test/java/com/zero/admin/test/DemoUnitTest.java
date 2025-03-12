package com.zero.admin.test;

import com.zero.admin.base.core.config.ZeroAdminConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * 单元测试案例
 *
 * @author Akai
 */
@SpringBootTest // 此注解只能在 springboot 主包下使用 需包含 main 方法与 yml 配置文件
@DisplayName("单元测试案例")
public class DemoUnitTest {

    @Autowired
    private ZeroAdminConfig zeroAdminConfig;

    @DisplayName("测试 @SpringBootTest @Test @DisplayName 注解")
    @Test
    void testTest() {
        System.out.println(zeroAdminConfig);
    }

    @Disabled
    @DisplayName("测试 @Disabled 注解")
    @Test
    void testDisabled() {
        System.out.println(zeroAdminConfig);
    }

    @Timeout(value = 2L, unit = TimeUnit.SECONDS)
    @DisplayName("测试 @Timeout 注解")
    @Test
    void testTimeout() throws InterruptedException {
        Thread.sleep(3000);
        System.out.println(zeroAdminConfig);
    }


    @DisplayName("测试 @RepeatedTest 注解")
    @RepeatedTest(3)
    void testRepeatedTest() {
        System.out.println(666);
    }

    @BeforeAll
    static void testBeforeAll() {
        System.out.println("@BeforeAll ==================");
    }

    @BeforeEach
    void testBeforeEach() {
        System.out.println("@BeforeEach ==================");
    }

    @AfterEach
    void testAfterEach() {
        System.out.println("@AfterEach ==================");
    }

    @AfterAll
    static void testAfterAll() {
        System.out.println("@AfterAll ==================");
    }

}

package com.zero.admin.job.config;

import com.aizuda.snailjob.client.starter.EnableSnailJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SnailJob 客户端开关。
 *
 * @author Akai
 */
@Configuration
@ConditionalOnProperty(prefix = "snail-job", name = "enabled", havingValue = "true")
@EnableScheduling
@EnableSnailJob
public class SnailJobConfig {

    // 注意：如果数据源初始化直接崩溃，这个注入有可能来不及执行
    @Autowired
    private Environment env;

    // 构造器方式，配置类实例化时打印
    public SnailJobConfig(Environment env) {
        System.out.println("\n=====SnailJobConfig读取配置=====");
        System.out.println("username:" + env.getProperty("spring.datasource.username"));
        System.out.println("url:" + env.getProperty("spring.datasource.url"));
        System.out.println("===============================\n");
    }
}
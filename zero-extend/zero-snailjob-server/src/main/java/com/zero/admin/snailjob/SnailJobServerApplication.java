package com.zero.admin.snailjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SnailJob 调度中心启动入口。
 *
 * @author Akai
 */
@SpringBootApplication
public class SnailJobServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(com.aizuda.snailjob.server.SnailJobServerApplication.class, args);
    }
}

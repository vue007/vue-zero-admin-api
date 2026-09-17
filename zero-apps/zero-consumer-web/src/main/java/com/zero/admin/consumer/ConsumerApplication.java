package com.zero.admin.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** C 端会员服务启动入口。 */
@SpringBootApplication(scanBasePackages = {
    "com.zero.admin.consumer",
    "com.zero.admin.member",
    "com.zero.admin.community",
    "com.zero.admin.tenantapp"
})
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}

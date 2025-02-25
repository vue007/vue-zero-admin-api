package com.zero.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication
public class AdminApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(AdminApplication.class);
		application.setApplicationStartup(new BufferingApplicationStartup(2048));
		application.run(args);

		System.out.println("(*^_-*)  Zero Admin 启动成功   ლ(-_^ლ)ﾞ");
	}
}

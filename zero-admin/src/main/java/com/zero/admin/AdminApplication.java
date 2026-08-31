package com.zero.admin;

import com.zero.admin.bootstrap.LocalRedisBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.env.Profiles;

@SpringBootApplication
public class AdminApplication {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(AdminApplication.class);
		application.setApplicationStartup(new BufferingApplicationStartup(2048));
		application.addInitializers(context -> {
			if (context.getEnvironment().acceptsProfiles(Profiles.of("local", "dev"))) {
				LocalRedisBootstrap.start();
			}
		});
		application.run(args);

		System.out.println("(*^_-*)  Zero Admin 启动成功   ლ(-_^ლ)ﾞ");
	}
}

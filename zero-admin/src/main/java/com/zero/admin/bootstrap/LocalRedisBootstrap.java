package com.zero.admin.bootstrap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 本地开发环境 Redis 启动器。
 */
public final class LocalRedisBootstrap {

	private static final String REDIS_HOST = "127.0.0.1";
	private static final int REDIS_PORT = 6379;
	private static final String REDIS_PASSWORD = "redis123";

	private LocalRedisBootstrap() {
	}

	public static void start() {
		if (!isWindows()) {
			System.out.println("本地 Redis 自动启动仅支持 Windows，已跳过");
			return;
		}

		try {
			if (!isRedisAvailable()) {
				runCommand(Duration.ofSeconds(10), "sc.exe", "start", "Redis");
				waitForRedis(Duration.ofSeconds(15));
			}

			// -a 等价于连接后先执行 AUTH，再设置 requirepass。
			runCommand(Duration.ofSeconds(5), "redis-cli", "-h", REDIS_HOST,
				"-p", String.valueOf(REDIS_PORT), "-a", REDIS_PASSWORD,
				"CONFIG", "SET", "requirepass", REDIS_PASSWORD);
			System.out.println("本地 Redis 已就绪");
		} catch (Exception exception) {
			throw new IllegalStateException("本地 Redis 启动或认证配置失败", exception);
		}
	}

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().contains("win");
	}

	private static boolean isRedisAvailable() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(REDIS_HOST, REDIS_PORT), 500);
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	private static void waitForRedis(Duration timeout) throws InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		while (System.nanoTime() < deadline) {
			if (isRedisAvailable()) {
				return;
			}
			Thread.sleep(250);
		}
		throw new IllegalStateException("等待本地 Redis 启动超时");
	}

	private static String runCommand(Duration timeout, String... command)
		throws IOException, InterruptedException {
		Process process = new ProcessBuilder(command)
			.redirectErrorStream(true)
			.start();
		boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
		if (!completed) {
			process.destroyForcibly();
			throw new IllegalStateException("命令执行超时: " + command[0]);
		}
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
		if (process.exitValue() != 0) {
			throw new IllegalStateException("命令执行失败: " + command[0] + ", " + output);
		}
		return output;
	}
}

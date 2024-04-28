package com.gmoon.springdataredis.test;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.Architecture;
import redis.embedded.util.OS;

@Slf4j
@TestConfiguration
public class EmbeddedRedisConfig implements InitializingBean, DisposableBean {
	private RedisServer redisServer;

	public EmbeddedRedisConfig(@Value("${spring.redis.port:4000}") int port) throws IOException {
		log.info("Embedded redis server. spring.redis.port : {}", port);

		try {
			this.redisServer = new RedisServer(getRedisServerFile(), port);
		} catch (Exception e) {
			RedisExecProvider provider = getRedisExecProvider();
			redisServer = new RedisServer(provider, port);
			throw new RuntimeException(e);
		}
	}

	private RedisExecProvider getRedisExecProvider() {
		RedisExecProvider provider = RedisExecProvider.defaultProvider();

		if (isArmMac()) {
			provider.override(OS.MAC_OS_X, Architecture.x86, "redis/redis-server-724-mac-arm64")
				.override(OS.MAC_OS_X, Architecture.x86_64, "redis/redis-server-724-mac-arm64");
		}
		return provider;
	}

	private boolean isArmMac() {
		return "aarch64".equals(System.getProperty("os.arch"))
			&& "Mac OS X".equals(System.getProperty("os.name"));
	}

	private File getRedisServerFile() throws IOException {
		return new ClassPathResource("/redis/redis-server-724-mac-arm64")
			.getFile();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Embedded redis server start.");
		redisServer.start();
	}

	@Override
	public void destroy() throws Exception {
		log.info("Embedded redis server stop.");
		redisServer.stop();
	}
}

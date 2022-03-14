package com.gmoon.springdataredis.test;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;

import lombok.extern.slf4j.Slf4j;

import redis.embedded.RedisServer;

@Slf4j
@TestConfiguration
public class EmbeddedRedisConfig implements InitializingBean, DisposableBean {
	private RedisServer redisServer;

	public EmbeddedRedisConfig(@Value("${spring.redis.port:4000}") int port) {
		log.info("Embedded redis server. spring.redis.port : {}", port);
		redisServer = new RedisServer(port);
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

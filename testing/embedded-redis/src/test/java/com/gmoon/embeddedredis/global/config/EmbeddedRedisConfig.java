package com.gmoon.embeddedredis.global.config;

import java.io.IOException;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestConfiguration
public class EmbeddedRedisConfig {

	@Configuration
	protected static class RedisServerConfig {

		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnProperty(value = "env", havingValue = "local")
		public redis.embedded.RedisServer redisServer(RedisProperties redisProperties) throws IOException {
			int port = redisProperties.getPort();
			log.info("embedded redis server(kstyrc) start. port: {}", port);
			return new redis.embedded.RedisServer(port);
		}
	}

	@Configuration
	@EnableRedisRepositories
	@AutoConfigureAfter(RedisServerConfig.class)
	protected static class RedisConnectionConfig {

		@Bean
		public LettuceConnectionFactory redisConnectionFactory(RedisConfiguration redisConfig) {
			LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
				// master-replica(master-slave) replication strategy config.
				.readFrom(ReadFrom.REPLICA_PREFERRED)
				.build();
			return new LettuceConnectionFactory(redisConfig, clientConfig);
		}

		@Bean
		public RedisConfiguration redisConfiguration(RedisProperties redisProperties) {
			String host = redisProperties.getHost();
			int port = redisProperties.getPort();
			return new RedisStandaloneConfiguration(host, port);
		}

		@Bean
		public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
			log.info("connectionFactory: {}", connectionFactory);
			RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(connectionFactory);
			redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
			return redisTemplate;
		}
	}
}

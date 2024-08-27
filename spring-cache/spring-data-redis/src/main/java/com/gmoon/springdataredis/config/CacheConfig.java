package com.gmoon.springdataredis.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springdataredis.cache.CachePolicy;
import com.gmoon.springdataredis.helper.RedisTemplateHelper;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig {

	/**
	 * RedisTemplate default key serializer is {@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}
	 * apply all serializers to key, value, hash key, hash value.
	 * {@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}
	 * {@link org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration}
	 * {@link org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration}
	 *
	 * @see RedisTemplate#afterPropertiesSet()
	 **/
	@Configuration
	@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
	@ConditionalOnProperty(value = "spring.cache.type", havingValue = "REDIS")
	@RequiredArgsConstructor
	static class RedisConfig {

		private static final String REDIS_SERVER_TYPE_CONFIG_PROPERTY = "spring.data.redis.server-type";

		/**
		 * <pre>
		 * ReadFrom									Read mode
		 * ************************************************************************************************
		 * MASTER / UPSTREAM						Read master node only
		 * MASTER_PREFERRED / UPSTREAM_PREFERRED	Read the master node first.
		 * If the master node is unavailable, read the slave node
		 * REPLICA / @Deprecated SLAVE				Read from node only
		 * REPLICA_PREFERRED / SLAVE_PREFERRED		Read the slave node first.
		 * If the slave node is unavailable, read the master node
		 * NEAREST									Read from nearest node
		 * ANY										Read from any node
		 * ANY_REPLICA								Read from any slave node
		 * </pre>
		 */
		@Bean
		public LettuceConnectionFactory redisConnectionFactory(RedisConfiguration redisConfig) {
			LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
				 // master-replica(master-slave) replication strategy config.
				 .readFrom(ReadFrom.REPLICA_PREFERRED).build();
			return new LettuceConnectionFactory(redisConfig, clientConfig);
		}

		@Bean
		@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.STANDALONE)
		public RedisConfiguration standaloneConfig(RedisProperties redisProperties) {
			String host = redisProperties.getHost();
			int port = redisProperties.getPort();
			return new RedisStandaloneConfiguration(host, port);
		}

		@Bean
		@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.SENTINEL)
		public RedisConfiguration sentinelConfig(RedisProperties redisProperties) {
			RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
			String nodeNameOfMaster = sentinel.getMaster();
			Set<String> hostAndPorts = new HashSet<>(sentinel.getNodes());
			return new RedisSentinelConfiguration(nodeNameOfMaster, hostAndPorts);
		}

		@Bean
		@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.ELASTI_CACHE)
		public RedisConfiguration elastiCacheConfig(RedisProperties redisProperties) {
			String host = redisProperties.getHost();
			int port = redisProperties.getPort();

			RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(host, port);
			setReplicaNodes(config, redisProperties.getCluster());
			return config;
		}

		private void setReplicaNodes(RedisStaticMasterReplicaConfiguration config, RedisProperties.Cluster cluster) {
			for (String hostAndPort : cluster.getNodes()) {
				String[] args = StringUtils.split(hostAndPort, ":");
				String replicaHost = args[0];
				int replicaPort = Integer.parseInt(args[1]);
				config.addNode(replicaHost, replicaPort);
			}
		}

		@Bean
		public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
			 RedisSerializer<?> redisSerializer) {
			log.info("connectionFactory: {}", connectionFactory);
			RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(connectionFactory);
			redisTemplate.setKeySerializer(RedisSerializer.string());
			redisTemplate.setValueSerializer(redisSerializer); // default RedisSerializer.java() (JdkSerializationRedisSerializer)
			return redisTemplate;
		}

		@Bean
		public RedisCacheManagerBuilderCustomizer cacheManagerBuilderCustomizer(CacheProperties cacheProperties,
			 RedisSerializer<?> redisSerializer) {
			var redis = cacheProperties.getRedis();
			return builder -> builder.withInitialCacheConfigurations(
					  cacheConfigurations(redis.getKeyPrefix(), redisSerializer))
				 .cacheDefaults(
					  cacheExpiresConfiguration(redis.getKeyPrefix(), redis.getTimeToLive(), redisSerializer));
		}

		private Map<String, RedisCacheConfiguration> cacheConfigurations(String keyPrefix,
			 RedisSerializer<?> redisSerializer) {
			return Arrays.stream(CachePolicy.values())
				 .collect(HashMap::new, (map, policy) -> map.put(policy.name,
					  cacheExpiresConfiguration(keyPrefix, policy.ttl, redisSerializer)), HashMap::putAll);
		}

		private RedisCacheConfiguration cacheExpiresConfiguration(String keyPrefix, Duration duration,
			 RedisSerializer<?> redisSerializer) {
			return RedisCacheConfiguration.defaultCacheConfig(Thread.currentThread().getContextClassLoader())
				 .prefixCacheNameWith(keyPrefix)
				 .entryTtl(duration)
				 .disableCachingNullValues()
				 .serializeKeysWith(
					  RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
				 .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
		}

		@Bean
		public RedisSerializer<Object> jsonRedisSerializer() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
			objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
				 ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

			return new GenericJackson2JsonRedisSerializer(objectMapper);
		}

		@Bean
		public RedisTemplateHelper redisTemplateHelper(RedisTemplate<String, Object> redisTemplate) {
			return new RedisTemplateHelper(redisTemplate);
		}
	}
}

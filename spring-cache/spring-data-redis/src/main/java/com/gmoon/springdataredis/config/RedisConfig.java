package com.gmoon.springdataredis.config;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springdataredis.cache.CacheName;
import com.gmoon.springdataredis.util.RedisUtils;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RedisTemplate default key serializer is {@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}
 * apply all serializers to key, value, hash key, hash value.
 * {@link org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration}
 * {@link org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration}
 * {@link org.springframework.boot.autoconfigure.data.redis.JedisConnectionConfiguration}
 *
 * @see RedisTemplate#afterPropertiesSet()
 **/
@Slf4j
@Configuration
@EnableCaching
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {
	private static final int DEFAULT_CACHE_EXPIRE_SECONDS = 30;
	private static final String REDIS_SERVER_TYPE_CONFIG_PROPERTY = "spring.data.redis.server-type";

	private final RedisProperties redisProperties;

	@Bean
	public LettuceConnectionFactory redisConnectionFactory(RedisConfiguration redisConfig) {
		log.info("redisConfig : {}", redisConfig.getClass());

		LettuceClientConfiguration clientConfig = getClientConfig();
		return new LettuceConnectionFactory(redisConfig, clientConfig);
	}

	@Bean
	@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.STANDALONE)
	public RedisConfiguration standaloneConfig() {
		String host = redisProperties.getHost();
		int port = redisProperties.getPort();
		return new RedisStandaloneConfiguration(host, port);
	}

	@Bean
	@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.SENTINEL)
	public RedisConfiguration sentinelConfig() {
		RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
		String nodeNameOfMaster = sentinel.getMaster();
		Set<String> hostAndPorts = new HashSet<>(sentinel.getNodes());
		return new RedisSentinelConfiguration(nodeNameOfMaster, hostAndPorts);
	}

	@Bean
	@ConditionalOnProperty(name = REDIS_SERVER_TYPE_CONFIG_PROPERTY, havingValue = RedisServerType.ELASTI_CACHE)
	public RedisConfiguration elastiCacheConfig() {
		String host = redisProperties.getHost();
		int port = redisProperties.getPort();

		RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(host, port);
		setReplicaNodes(config);
		return config;
	}

	private void setReplicaNodes(RedisStaticMasterReplicaConfiguration config) {
		RedisProperties.Cluster cluster = redisProperties.getCluster();
		for (String hostAndPort : cluster.getNodes()) {
			String[] args = StringUtils.split(hostAndPort, ":");
			String replicaHost = args[0];
			int replicaPort = Integer.parseInt(args[1]);
			config.addNode(replicaHost, replicaPort);
		}
	}

	/**
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
	 */
	private LettuceClientConfiguration getClientConfig() {
		return LettuceClientConfiguration.builder()
			 // master-replica(master-slave) replication strategy config.
			 .readFrom(ReadFrom.REPLICA_PREFERRED)
			 .build();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		log.info("connectionFactory: {}", connectionFactory);
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(getKeySerializer());
		// JdkSerializationRedisSerializer default
		// redisTemplate.setValueSerializer(RedisSerializer.java());
		redisTemplate.setValueSerializer(RedisSerializer.json());
		return redisTemplate;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// define all cache names
		Set<String> allCacheNames = CacheName.getAll();

		// cache ttl settings
		Map<String, RedisCacheConfiguration> cacheExpireConfigs = RedisCacheExpireConfigs.create()
			 .putExpireMinutes(CacheName.USER, 1)
			 .getValues();

		return RedisCacheManager.builder(connectionFactory)
			 .cacheDefaults(createRedisCacheConfig(Duration.ofSeconds(DEFAULT_CACHE_EXPIRE_SECONDS)))
			 .initialCacheNames(allCacheNames) // set default cache config
			 .withInitialCacheConfigurations(cacheExpireConfigs)
			 .transactionAware()
			 .build();
	}

	private RedisCacheConfiguration createRedisCacheConfig(Duration expireTtl) {
		return RedisUtils.createRedisCacheConfig(expireTtl);
	}

	private StringRedisSerializer getKeySerializer() {
		return RedisUtils.getKeySerializer();
	}
}

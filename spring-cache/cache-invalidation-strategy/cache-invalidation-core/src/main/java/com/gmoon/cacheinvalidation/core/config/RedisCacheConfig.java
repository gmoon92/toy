package com.gmoon.cacheinvalidation.core.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.JitteredTtl;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.FallbackCacheErrorHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheConfig implements CachingConfigurer {

	private final CacheFailureRecorder cacheFailureRecorder;

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new FallbackCacheErrorHandler(cacheFailureRecorder);
	}

	@Bean
	public RedisCacheWriter redisCacheWriter(
		 RedisConnectionFactory connectionFactory,
		 CacheInvalidationProperties properties
	) {
		return RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
			 BatchStrategies.scan(properties.clearScanBatchSize()));
	}

	@Bean
	public CacheSerializerFactory cacheSerializerFactory() {
		return new CacheSerializerFactory();
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer cachePolicyCustomizer(
		 CachePolicyRegistry registry,
		 CacheProperties cacheProperties,
		 CacheInvalidationProperties properties,
		 CacheSerializerFactory serializerFactory
	) {
		CacheProperties.Redis redis = cacheProperties.getRedis();
		JitteredTtl jitteredTtl = new JitteredTtl(properties.ttlJitterRatio());
		return builder -> builder
			 .disableCreateOnMissingCache()
			 .cacheDefaults(configurationOf(defaultTtlOf(redis), redis, properties, jitteredTtl,
				  serializerFactory.rejectingSerializer()))
			 .withInitialCacheConfigurations(
				  configurationsByCacheName(registry, redis, properties, jitteredTtl, serializerFactory));
	}

	private Duration defaultTtlOf(CacheProperties.Redis redis) {
		return redis.getTimeToLive() == null ? Duration.ofMinutes(5) : redis.getTimeToLive();
	}

	private Map<String, RedisCacheConfiguration> configurationsByCacheName(
		 CachePolicyRegistry registry,
		 CacheProperties.Redis redis,
		 CacheInvalidationProperties properties,
		 JitteredTtl jitteredTtl,
		 CacheSerializerFactory serializerFactory
	) {
		Map<String, RedisCacheConfiguration> configurations = new HashMap<>();
		for (CachePolicy policy : registry.all()) {
			configurations.put(policy.cacheName(),
				 configurationOf(policy.ttl(), redis, properties, jitteredTtl,
					  serializerFactory.serializerFor(policy)));
		}
		return configurations;
	}

	private RedisCacheConfiguration configurationOf(
		 Duration baseTtl,
		 CacheProperties.Redis redis,
		 CacheInvalidationProperties properties,
		 JitteredTtl jitteredTtl,
		 RedisSerializer<?> serializer
	) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .entryTtl(ttlFunctionOf(baseTtl, properties, jitteredTtl))
			 .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
			 .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

		return applyKeyPrefix(configuration, redis);
	}

	private RedisCacheConfiguration applyKeyPrefix(RedisCacheConfiguration configuration, CacheProperties.Redis redis) {
		if (!redis.isUseKeyPrefix()) {
			return configuration.disableKeyPrefix();
		}
		return redis.getKeyPrefix() == null
			 ? configuration
			 : configuration.prefixCacheNameWith(redis.getKeyPrefix());
	}

	private RedisCacheWriter.TtlFunction ttlFunctionOf(
		 Duration baseTtl,
		 CacheInvalidationProperties properties,
		 JitteredTtl jitteredTtl
	) {
		return (key, value) -> value instanceof NullValue
			 ? jitteredTtl.apply(properties.notFoundTtl())
			 : jitteredTtl.apply(baseTtl);
	}
}

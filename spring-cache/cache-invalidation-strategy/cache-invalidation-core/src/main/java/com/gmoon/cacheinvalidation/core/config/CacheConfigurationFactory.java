package com.gmoon.cacheinvalidation.core.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.serialization.SerializerFactory;

import lombok.RequiredArgsConstructor;

/**
 * 정책 하나를 Redis 캐시 설정 하나로 바꾼다.
 * <p>
 * 직렬화·만료·키 접두사는 캐시마다 같은 방식으로 조합되므로 이 타입이 그 조합을 쥐고 있는다.
 */
@RequiredArgsConstructor
public class CacheConfigurationFactory {

	private static final Duration FALLBACK_TTL = Duration.ofMinutes(5);

	private final SerializerFactory serializers;
	private final TtlResolver ttlResolver;
	private final CacheProperties.Redis redis;

	public RedisCacheConfiguration configurationOf(CachePolicy policy) {
		return configurationOf(policy.ttl(), serializers.valueSerializerFor(policy));
	}

	public RedisCacheConfiguration unregisteredCacheConfiguration() {
		return configurationOf(declaredTtl(), serializers.unregisteredCacheSerializer());
	}

	private RedisCacheConfiguration configurationOf(Duration declaredTtl, RedisSerializer<?> valueSerializer) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .entryTtl(ttlResolver.resolveFrom(declaredTtl))
			 .serializeKeysWith(SerializationPair.fromSerializer(serializers.keySerializer()))
			 .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));

		return applyKeyPrefix(configuration);
	}

	private RedisCacheConfiguration applyKeyPrefix(RedisCacheConfiguration configuration) {
		if (!redis.isUseKeyPrefix()) {
			return configuration.disableKeyPrefix();
		}
		return redis.getKeyPrefix() == null
			 ? configuration
			 : configuration.prefixCacheNameWith(redis.getKeyPrefix());
	}

	private Duration declaredTtl() {
		return redis.getTimeToLive() == null ? FALLBACK_TTL : redis.getTimeToLive();
	}
}

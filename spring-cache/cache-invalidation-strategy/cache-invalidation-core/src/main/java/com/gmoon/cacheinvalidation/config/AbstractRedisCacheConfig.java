package com.gmoon.cacheinvalidation.config;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.CachePolicy;
import com.gmoon.cacheinvalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.serializer.SerializerFactory;

import jakarta.annotation.PostConstruct;

/**
 * Redis 를 캐시 저장소로 쓰는 전략 모듈이 상속하는 설정의 기반이다.
 * <p>
 * 이 클래스는 무효화 파이프라인만 배선하고 변경 감지 방식은 정하지 않는다.
 * 무효화가 필요한 모듈은
 * {@link com.gmoon.cacheinvalidation.JpaEntityChangeConfig JpaEntityChangeConfig} 또는
 * {@link com.gmoon.cacheinvalidation.PublishedEntityChangeConfig PublishedEntityChangeConfig}
 * 를 함께 선언하고, TTL 로만 만료시키는 모듈은 아무것도 선언하지 않는다.
 * <p>
 * 감지 방식은 조합 가능한 선택이므로 상속이 아니라 {@code @Import} 로 켠다.
 * 자바는 단일 상속이라 JPA 와 이벤트를 함께 쓰는 모듈을 상속만으로 표현할 수 없다.
 *
 * <p>재정의 가능한 확장점
 * <ul>
 *     <li>{@link #cachePolicies()} — 필수. 이 모듈이 소유한 캐시 목록</li>
 *     <li>{@link #invalidationRules()} — 무효화 대상 산출 규칙</li>
 *     <li>{@link #serializerFactory()} — 직렬화 전략</li>
 *     <li>{@link #ttlResolver(ServiceCacheProperties)} — 만료 정책</li>
 * </ul>
 */
@EnableCaching
@EnableConfigurationProperties(ServiceCacheProperties.class)
public abstract class AbstractRedisCacheConfig implements CachingConfigurer {

	private static final Duration UNREGISTERED_FALLBACK_TTL = Duration.ofMinutes(5);

	protected abstract Collection<CachePolicy> cachePolicies();

	protected List<InvalidationRule> invalidationRules() {
		return List.of();
	}

	@Bean
	protected SerializerFactory serializerFactory() {
		return SerializerFactory.json();
	}

	@Bean
	protected TtlResolver ttlResolver(ServiceCacheProperties properties) {
		ServiceCacheProperties.Expiration expiration = properties.expiration();
		return TtlResolver.jittered(
			 expiration.jitterRatio(),
			 expiration.notFoundTtl()
		);
	}

	@PostConstruct
	protected void validateInvalidationOwnership() {
		List<InvalidationRule> rules = invalidationRules();
		if (rules.isEmpty()) {
			return;
		}
		InvalidationRule.verifyOwnership(rules, cachePolicies());
	}

	@Bean
	protected CacheInvalidator cacheInvalidator(CacheManager cacheManager) {
		return CacheInvalidator.of(invalidationRules(), cacheManager);
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new FallbackCacheErrorHandler();
	}

	@Bean
	protected RedisCacheWriter redisCacheWriter(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		return RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
			 BatchStrategies.scan(properties.invalidation().clearScanBatchSize()));
	}

	@Bean
	protected RedisCacheManagerBuilderCustomizer cachePolicyCustomizer(
		 SerializerFactory serializers,
		 TtlResolver ttlResolver,
		 CacheProperties cacheProperties
	) {
		CacheProperties.Redis redis = cacheProperties.getRedis();

		Map<String, RedisCacheConfiguration> byCacheName = CachePolicy.distinct(cachePolicies())
			 .stream()
			 .collect(Collectors.toMap(
					   CachePolicy::cacheName,
					   policy -> configurationOf(
							policy.ttl(),
							serializers.valueSerializerFor(policy),
							serializers,
							ttlResolver,
							redis
					   )
				  )
			 );

		RedisCacheConfiguration unregistered = configurationOf(
			 redis.getTimeToLive() == null ? UNREGISTERED_FALLBACK_TTL : redis.getTimeToLive(),
			 serializers.unregisteredCacheSerializer(), serializers, ttlResolver, redis);

		return builder -> builder
			 .disableCreateOnMissingCache()
			 .cacheDefaults(unregistered)
			 .withInitialCacheConfigurations(byCacheName);
	}

	private RedisCacheConfiguration configurationOf(
		 Duration declaredTtl,
		 RedisSerializer<?> valueSerializer,
		 SerializerFactory serializers,
		 TtlResolver ttlResolver,
		 CacheProperties.Redis redis
	) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .entryTtl(ttlResolver.resolveFrom(declaredTtl))
			 .serializeKeysWith(SerializationPair.fromSerializer(serializers.keySerializer()))
			 .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));

		if (!redis.isUseKeyPrefix()) {
			return configuration.disableKeyPrefix();
		}
		return redis.getKeyPrefix() == null
			 ? configuration
			 : configuration.prefixCacheNameWith(redis.getKeyPrefix());
	}
}

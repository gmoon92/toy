package com.gmoon.cacheinvalidation.core.config;

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

import com.gmoon.cacheinvalidation.core.expiration.JitteredTtlResolver;
import com.gmoon.cacheinvalidation.core.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.invalidation.CacheEvictor;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.FailSafeCacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationOwnership;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRuleSet;
import com.gmoon.cacheinvalidation.core.invalidation.RuleBasedCacheInvalidator;
import com.gmoon.cacheinvalidation.core.policy.CacheCatalog;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.resilience.FallbackCacheErrorHandler;
import com.gmoon.cacheinvalidation.core.serialization.JsonSerializerFactory;
import com.gmoon.cacheinvalidation.core.serialization.SerializerFactory;

import jakarta.annotation.PostConstruct;

/**
 * Redis 를 캐시 저장소로 쓰는 전략 모듈이 상속하는 설정의 기반이다.
 * <p>
 * 이 클래스는 무효화 파이프라인만 배선하고 변경 감지 방식은 정하지 않는다.
 * 무효화가 필요한 모듈은
 * {@link com.gmoon.cacheinvalidation.core.config.JpaEntityChangeConfig JpaEntityChangeConfig} 또는
 * {@link com.gmoon.cacheinvalidation.core.config.ApplicationEventChangeConfig ApplicationEventChangeConfig}
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


	protected abstract Collection<CachePolicy> cachePolicies();

	protected List<InvalidationRule> invalidationRules() {
		return List.of();
	}

	@Bean
	public SerializerFactory serializerFactory() {
		return new JsonSerializerFactory();
	}

	@Bean
	public TtlResolver ttlResolver(ServiceCacheProperties properties) {
		return new JitteredTtlResolver(properties.expiration().jitterRatio(),
			 properties.expiration().notFoundTtl());
	}

	@PostConstruct
	public void validateInvalidationOwnership() {
		List<InvalidationRule> rules = invalidationRules();
		if (rules.isEmpty()) {
			return;
		}
		InvalidationOwnership.of(rules).verifyCovers(cachePolicies());
	}

	@Bean
	public CacheCatalog cachePolicyRegistry() {
		return new CacheCatalog(cachePolicies());
	}

	@Bean
	public InvalidationRuleSet invalidationRuleSet(InvalidationRecorder recorder) {
		return new InvalidationRuleSet(invalidationRules(), recorder);
	}

	@Bean
	public CacheInvalidator cacheInvalidator(
		 InvalidationRuleSet rules,
		 CacheEvictor cacheEvictor,
		 InvalidationRecorder recorder
	) {
		return new FailSafeCacheInvalidator(new RuleBasedCacheInvalidator(rules, cacheEvictor, recorder), recorder);
	}

	@Bean
	public CacheEvictor cacheEvictor(CacheManager cacheManager) {
		return new CacheEvictor(cacheManager);
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new FallbackCacheErrorHandler(cacheFailureRecorder());
	}

	@Bean
	public CacheFailureRecorder cacheFailureRecorder() {
		return new CacheFailureRecorder();
	}

	@Bean
	public InvalidationRecorder invalidationRecorder() {
		return new InvalidationRecorder();
	}

	@Bean
	public RedisCacheWriter redisCacheWriter(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		return RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
			 BatchStrategies.scan(properties.invalidation().clearScanBatchSize()));
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer cachePolicyCustomizer(
		 CacheCatalog catalog,
		 SerializerFactory serializers,
		 TtlResolver ttlResolver,
		 CacheProperties cacheProperties
	) {
		CacheConfigurationFactory configurations =
			 new CacheConfigurationFactory(serializers, ttlResolver, cacheProperties.getRedis());

		Map<String, RedisCacheConfiguration> byCacheName = catalog.all()
			 .stream()
			 .collect(Collectors.toMap(CachePolicy::cacheName, configurations::configurationOf));

		return builder -> builder
			 .disableCreateOnMissingCache()
			 .cacheDefaults(configurations.unregisteredCacheConfiguration())
			 .withInitialCacheConfigurations(byCacheName);
	}
}

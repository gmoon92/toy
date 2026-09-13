package com.gmoon.cacheinvalidation.core.config;

import java.util.Collection;
import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.gmoon.cacheinvalidation.core.cache.eviction.CacheEvictor;
import com.gmoon.cacheinvalidation.core.cache.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.serialization.SerializerFactory;
import com.gmoon.cacheinvalidation.core.cache.expiration.JitteredTtlResolver;
import com.gmoon.cacheinvalidation.core.cache.serialization.JsonSerializerFactory;
import com.gmoon.cacheinvalidation.core.invalidation.CacheOwnershipValidator;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRuleSet;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.RuleBasedCacheInvalidator;
import com.gmoon.cacheinvalidation.core.cache.resilience.CacheFailureRecorder;
import com.gmoon.cacheinvalidation.core.cache.resilience.FallbackCacheErrorHandler;
import com.gmoon.cacheinvalidation.core.invalidation.metrics.InvalidationRecorder;

/**
 * Redis 를 캐시 저장소로 쓰는 전략 모듈이 상속하는 설정의 기반이다.
 * <p>
 * 이름이 밝히듯 <strong>Redis 전용</strong>이다. 다른 저장소를 쓸 때 이 클래스를 일반화하지 말고
 * 형제 기반 클래스를 따로 두고, 공통이 실제로 드러난 뒤에 뽑아낸다.
 * <p>
 * 이 클래스는 <strong>무효화 파이프라인만</strong> 배선한다.
 * 변경을 무엇으로 감지할지는 정하지 않으므로, 무효화가 필요한 모듈은
 * {@link JpaEntityChangeConfig} 또는 {@link ApplicationEventChangeConfig} 를 함께 선언한다.
 * TTL 로만 만료시키는 모듈은 아무것도 선언하지 않는다.
 *
 * <p>변경 감지 방식은 조합 가능한 선택이므로 상속이 아니라 {@code @Import} 로 켠다.
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

	@Bean
	public CachePolicyRegistry cachePolicyRegistry() {
		return new CachePolicyRegistry(cachePolicies());
	}

	@Bean
	public InvalidationRuleSet invalidationRules(InvalidationRecorder recorder) {
		return new InvalidationRuleSet(invalidationRules(), recorder);
	}

	@Bean
	public CacheOwnershipValidator cacheOwnershipValidator(
		 CachePolicyRegistry registry,
		 InvalidationRuleSet rules
	) {
		return new CacheOwnershipValidator(registry, rules);
	}

	@Bean
	public CacheInvalidator cacheInvalidator(
		 InvalidationRuleSet rules,
		 CacheEvictor cacheEvictor,
		 InvalidationRecorder recorder
	) {
		return new RuleBasedCacheInvalidator(rules, cacheEvictor, recorder);
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
	public RedisCacheSettings redisCacheConfigurations(
		 SerializerFactory serialization,
		 TtlResolver expiration,
		 CacheProperties cacheProperties
	) {
		return new RedisCacheSettings(serialization, expiration, cacheProperties.getRedis());
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer cachePolicyCustomizer(
		 CachePolicyRegistry registry,
		 RedisCacheSettings redisCacheSettings
	) {
		return builder -> builder
			 .disableCreateOnMissingCache()
			 .cacheDefaults(redisCacheSettings.unregisteredCacheDefaults())
			 .withInitialCacheConfigurations(redisCacheSettings.byCacheName(registry));
	}
}

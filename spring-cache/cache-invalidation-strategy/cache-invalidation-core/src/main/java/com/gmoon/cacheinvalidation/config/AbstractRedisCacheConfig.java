package com.gmoon.cacheinvalidation.config;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.util.function.SingletonSupplier;

import io.micrometer.core.instrument.MeterRegistry;

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
 * {@link JpaEntityChangeConfig} 또는
 * {@link PublishedEntityChangeConfig}
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
 *     <li>{@link #evictionExecutor()} — 커밋 이후 캐시 삭제를 실행할 곳</li>
 * </ul>
 */
@EnableCaching
@EnableConfigurationProperties(ServiceCacheProperties.class)
public abstract class AbstractRedisCacheConfig implements CachingConfigurer {

	/**
	 * 확장점은 한 번만 평가해 고정한다.
	 * <p>
	 * 호출할 때마다 새로 만들면 기동 검증이 본 규칙과 런타임이 쓰는 규칙이 서로 다른 객체가 된다.
	 */
	private final Supplier<Collection<CachePolicy>> policies =
		 SingletonSupplier.of(() -> CachePolicy.distinct(cachePolicies()));
	private final Supplier<List<InvalidationRule>> rules =
		 SingletonSupplier.of(() -> List.copyOf(invalidationRules()));

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
		if (rules.get().isEmpty()) {
			return;
		}
		InvalidationRule.verifyOwnership(
			 rules.get(),
			 policies.get()
		);
	}

	/**
	 * 커밋 이후 캐시 삭제를 실행할 곳. 기본은 커밋한 스레드다.
	 * <p>
	 * 다른 실행기로 바꾸면 커밋한 스레드가 Redis 왕복을 기다리지 않는 대신,
	 * 저장 직후의 조회가 삭제 전의 옛 값을 읽을 수 있다.
	 * 반환한 실행기의 생성과 종료는 재정의한 쪽이 관리한다.
	 */
	protected Executor evictionExecutor() {
		return Runnable::run;
	}

	@Bean
	protected CacheInvalidator cacheInvalidator(
		 CacheManager cacheManager,
		 ObjectProvider<MeterRegistry> meterRegistry
	) {
		return CacheInvalidator.of(
			 rules.get(),
			 cacheManager,
			 evictionExecutor(),
			 meterRegistry.getIfAvailable()
		);
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new FallbackCacheErrorHandler();
	}

	@Bean
	protected RedisCacheManagerBuilderCustomizer cacheManagerCustomizer(
		 SerializerFactory serializers,
		 TtlResolver ttlResolver,
		 CacheProperties cacheProperties,
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		rejectCacheNamesWithoutPolicy(cacheProperties.getCacheNames(), declaredCacheNames());

		return builder -> {
			Map<String, RedisCacheConfiguration> byCacheName = configurationsOver(
				 builder.cacheDefaults(),
				 serializers,
				 ttlResolver
			);
			builder.cacheWriter(scanningCacheWriter(connectionFactory, properties))
				 .disableCreateOnMissingCache()
				 .withInitialCacheConfigurations(byCacheName);
		};
	}

	private List<String> declaredCacheNames() {
		return policies.get().stream().map(CachePolicy::cacheName).toList();
	}

	/**
	 * Redis 캐시 기본 설정을 바탕으로 정책별 설정을 만든다.
	 * <p>
	 * 기본 설정은 Boot 가 {@code spring.cache.redis.*} 로 만들거나,
	 * 애플리케이션이 등록한 {@link RedisCacheConfiguration} 빈이다.
	 * 정책은 TTL·직렬화만 덮어쓰고 키 접두사·null 캐싱 같은 나머지는 기본 설정을 물려받는다.
	 */
	private Map<String, RedisCacheConfiguration> configurationsOver(
		 RedisCacheConfiguration defaults,
		 SerializerFactory serializers,
		 TtlResolver ttlResolver
	) {
		Duration defaultTtl = defaults.getTtlFunction().getTimeToLive(Object.class, null);
		return policies.get()
			 .stream()
			 .collect(Collectors.toMap(
				  CachePolicy::cacheName,
				  policy -> defaults
					   .entryTtl(ttlResolver.functionFor(ttlOf(policy, defaultTtl)))
					   .serializeKeysWith(SerializationPair.fromSerializer(serializers.keySerializer()))
					   .serializeValuesWith(SerializationPair.fromSerializer(serializers.valueSerializerFor(policy)))
			 ));
	}

	/**
	 * 정책이 TTL 을 정하지 않았고 기본 설정에도 TTL 이 없으면 기동을 막는다.
	 * Redis 는 0 이하 TTL 을 "만료 없음" 으로 다뤄 키가 영구히 남는다.
	 */
	private static Duration ttlOf(CachePolicy policy, Duration defaultTtl) {
		return policy.ttl().orElseGet(() -> {
			if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) {
				throw new IllegalStateException("Cache '" + policy.cacheName() + "' declares no ttl and the redis cache"
					 + " defaults have none. Declare a ttl on the CachePolicy, or set spring.cache.redis.time-to-live.");
			}
			return defaultTtl;
		});
	}

	/**
	 * {@code spring.cache.cache-names} 는 커스터마이저보다 먼저 적용된다.
	 * <p>
	 * Boot 가 {@code initialCacheNames} 로 그때의 기본 설정(JDK 직렬화)을 복사해 두므로
	 * 정책 없는 이름이 섞이면 {@code disableCreateOnMissingCache} 에도 걸리지 않고 살아남는다.
	 */
	private void rejectCacheNamesWithoutPolicy(
		 Collection<String> requestedCacheNames,
		 Collection<String> declaredCacheNames
	) {
		List<String> withoutPolicy = requestedCacheNames.stream()
			 .filter(name -> !declaredCacheNames.contains(name))
			 .toList();

		if (!withoutPolicy.isEmpty()) {
			throw new IllegalStateException(
				 "spring.cache.cache-names declares caches without a CachePolicy: " + withoutPolicy
					  + ". Declare a CachePolicy, or remove them from the property.");
		}
	}

	private RedisCacheWriter scanningCacheWriter(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		return RedisCacheWriter.nonLockingRedisCacheWriter(
			 connectionFactory,
			 BatchStrategies.scan(properties.clear().scanBatchSize())
		);
	}
}

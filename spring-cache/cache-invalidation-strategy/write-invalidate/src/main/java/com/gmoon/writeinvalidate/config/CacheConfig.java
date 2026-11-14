package com.gmoon.writeinvalidate.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

import io.micrometer.core.instrument.MeterRegistry;

import com.gmoon.writeinvalidate.cache.CacheEvictListener;
import com.gmoon.writeinvalidate.cache.CacheEvictable;

/**
 * 캐시 관리자에 정책을 얹고, 캐시를 지우는 리스너를 등록한다.
 * 그 리스너를 하이버네이트에 붙이는 일은 {@link JpaConfig} 가 맡는다.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(ServiceCacheProperties.class)
@RequiredArgsConstructor
public class CacheConfig implements CachingConfigurer {

	private final CacheProperties cacheProperties;

	/**
	 * {@code spring.cache.cache-names} 는 커스터마이저보다 먼저 적용된다.
	 * <p>
	 * Boot 가 {@link RedisCacheManagerBuilder#initialCacheNames(java.util.Set) initialCacheNames} 로
	 * 그때의 기본 설정(JDK 직렬화)을 복사해 두므로, 정책 없는 이름이 섞이면
	 * {@link RedisCacheManagerBuilder#disableCreateOnMissingCache() disableCreateOnMissingCache} 에도
	 * 걸리지 않고 살아남는다.
	 * <p>
	 * 이 설정은 {@link CacheManager} 빈을 직접 만들지 않는다. Boot 가 만드는 빌더를 커스터마이저로 고칠 뿐이고,
	 * Boot 는 그 빌더에 {@code cache-names} 를 먼저 등록한 뒤 커스터마이저를 부른다
	 * ({@code org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration#cacheManager}).
	 * {@link CacheManager} 빈을 직접 등록하면 Boot 가 물러나 이 속성은 무시되지만,
	 * {@code spring.cache.redis.*} 처리까지 함께 사라진다.
	 */
	@PostConstruct
	void rejectCacheNamesProperty() {
		List<String> requestedCacheNames = cacheProperties.getCacheNames();
		if (!requestedCacheNames.isEmpty()) {
			throw new IllegalStateException("spring.cache.cache-names is not supported: " + requestedCacheNames
				 + ". Cache names come from CachePolicy only.");
		}
	}

	/**
	 * 직접 발행한 {@link CacheEvictable} 엔티티는 이 빈의 {@code @TransactionalEventListener} 메서드가 받는다.
	 */
	@Bean
	public CacheEvictListener cacheEvictListener(
		 CacheManager cacheManager,
		 ObjectProvider<MeterRegistry> meterRegistry
	) {
		return new CacheEvictListener(cacheManager, meterRegistry.getIfAvailable());
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		return builder -> builder
			 .cacheWriter(scanningCacheWriter(connectionFactory, properties))
			 .disableCreateOnMissingCache()
			 .withInitialCacheConfigurations(
				  configurationsOver(List.of(CachePolicy.values()), builder.cacheDefaults())
			 );
	}

	/**
	 * {@link CachePolicy} 는 TTL·값 직렬화만 덮어쓰고, 키 접두사·null 캐싱 같은 나머지는
	 * {@link RedisCacheManagerBuilder#cacheDefaults() Redis 캐시 기본 설정} 을 물려받는다.
	 */
	private Map<String, RedisCacheConfiguration> configurationsOver(
		 Collection<CachePolicy> policies,
		 RedisCacheConfiguration defaults
	) {
		return policies.stream()
			 .collect(Collectors.toMap(
				  CachePolicy::cacheName,
				  policy -> defaults
					   .entryTtl(policy.ttl())
					   .serializeValuesWith(valueSerializationFor(policy))
			 ));
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

	/**
	 * 캐시마다 {@link CachePolicy#valueType() 값 타입} 을 고정해 {@code @class} 같은 타입 힌트 없이 저장한다.
	 * <p>
	 * 애플리케이션의 {@link ObjectMapper} 를 공유하지 않는다.
	 * 이름 전략 같은 전역 설정이 바뀌면 이미 저장된 캐시 값을 읽지 못하게 된다.
	 */
	private SerializationPair<?> valueSerializationFor(CachePolicy policy) {
		ObjectMapper cacheValueMapper = new ObjectMapper()
			 .registerModule(new JavaTimeModule())
			 .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<?> serializer = new Jackson2JsonRedisSerializer<>(
			 cacheValueMapper,
			 policy.valueType()
		);
		return SerializationPair.fromSerializer(serializer);
	}

	/**
	 * 캐시 접근이 실패해도 원본 조회로 넘어가게 한다.
	 * <p>
	 * 스프링 기본 처리기({@link SimpleCacheErrorHandler})는 예외를 그대로 전파해
	 * Redis 장애가 곧 서비스 장애가 된다.
	 * {@link LoggingCacheErrorHandler} 는 기본값으로 스택 트레이스를 남기지 않아 실패 원인이 로그에서 빠진다.
	 */
	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		boolean logStackTraces = true;
		return new LoggingCacheErrorHandler(logStackTraces);
	}
}

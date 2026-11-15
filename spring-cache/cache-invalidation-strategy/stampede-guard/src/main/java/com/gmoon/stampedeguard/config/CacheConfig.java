package com.gmoon.stampedeguard.config;

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

/**
 * 캐시 관리자에 정책을 얹는다.
 * 무효화 직후 MISS 가 몰릴 때 원본 조회를 한 번으로 줄이는 것은 락을 거는 캐시 writer 가 맡는다.
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(ServiceCacheProperties.class)
@RequiredArgsConstructor
public class CacheConfig implements CachingConfigurer {

	private final CacheProperties cacheProperties;

	/**
	 * {@code spring.cache.cache-names} 를 적으면 기동을 막는다. 캐시 이름은 {@link CachePolicy} 에서만 온다.
	 * <p>
	 * 이 속성은 커스터마이저보다 먼저 적용된다.
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
	 *
	 * <p><b>왜 커스터마이저가 아니라 {@code @PostConstruct} 인가</b>
	 * <p>
	 * 이 검사는 설정이 올바른지 따지는 일이고, 커스터마이저는 빌더를 고치는 일이다. 둘을 나눈다.
	 * 실패 시점은 둘 다 {@link CacheManager} 생성 전이라 막는 효과는 같다.
	 *
	 * <p><b>1. 컨텍스트 기동 순서</b> (spring-context {@code AbstractApplicationContext#refresh})
	 * <ol>
	 *   <li>{@code invokeBeanFactoryPostProcessors} — {@code ConfigurationClassPostProcessor} 가
	 *   {@code @Configuration}·{@code @Bean} 을 읽어 <b>빈 정의만</b> 등록한다. 객체는 아직 하나도 없다.
	 *   {@code @Configuration} 클래스는 이때 CGLIB 하위 클래스로 바뀌고,
	 *   자동 설정의 {@code @EnableConfigurationProperties(CacheProperties.class)} 도 정의로 등록된다.</li>
	 *   <li>{@code registerBeanPostProcessors} — {@code @PostConstruct} 를 처리할
	 *   {@code CommonAnnotationBeanPostProcessor} 같은 후처리기를 먼저 만든다.</li>
	 *   <li>{@code finishBeanFactoryInitialization} — 남은 싱글톤을 만든다. 아래 2·3 이 여기서 일어난다.</li>
	 * </ol>
	 *
	 * <p><b>2. 이 설정 빈 하나가 만들어지는 순서</b>
	 * (spring-beans {@code AbstractAutowireCapableBeanFactory#doCreateBean}, {@code #initializeBean})
	 * <ol>
	 *   <li>생성자 호출 — {@link CacheProperties} 를 주입받는다.
	 *   이때 {@code CacheProperties} 빈이 먼저 만들어지며 {@code spring.cache.*} 가 바인딩된다.</li>
	 *   <li>필드·세터 주입 ({@code populateBean})</li>
	 *   <li>Aware 콜백 ({@code BeanNameAware}, {@code BeanFactoryAware} 등)</li>
	 *   <li>{@code BeanPostProcessor#postProcessBeforeInitialization} —
	 *   <b>{@code @PostConstruct} 가 여기서 실행된다. 이 검사가 이 단계다</b>
	 *   ({@code InitDestroyAnnotationBeanPostProcessor})</li>
	 *   <li>{@code InitializingBean#afterPropertiesSet}, 그다음 init-method ({@code #invokeInitMethods})</li>
	 *   <li>{@code BeanPostProcessor#postProcessAfterInitialization} — AOP 프록시 등을 씌운다</li>
	 * </ol>
	 *
	 * <p><b>3. 커스터마이저와 캐시 관리자가 만들어지는 순서</b>
	 * (spring-boot-autoconfigure {@code RedisCacheConfiguration#cacheManager})
	 * <ol>
	 *   <li>Boot 가 캐시 관리자 빈을 만들기 시작한다.</li>
	 *   <li>빌더에 {@code spring.cache.redis.*} 기본 설정을 넣고,
	 *   {@code cache-names} 가 있으면 {@code initialCacheNames} 로 먼저 등록한다.</li>
	 *   <li>{@code ObjectProvider<RedisCacheManagerBuilderCustomizer>} 를 꺼내는 순간
	 *   {@link #cacheManagerCustomizer} 빈이 만들어진다. {@code @Bean} 메서드는 설정 빈을 {@code getBean} 으로 받아 호출하므로
	 *   ({@code ConstructorResolver#instantiateUsingFactoryMethod}), 설정 빈의 2번 순서가 모두 끝난 뒤다.</li>
	 *   <li>{@code customize(builder)} → {@code builder.build()} → {@link CacheManager}</li>
	 * </ol>
	 * 다른 빈이 이 설정 빈을 먼저 요청하면 더 일찍 만들어질 수 있다.
	 * 그래도 {@code @PostConstruct} 가 {@code @Bean} 메서드 호출보다 앞선다는 순서는 바뀌지 않는다.
	 */
	@PostConstruct
	void rejectCacheNamesProperty() {
		List<String> requestedCacheNames = cacheProperties.getCacheNames();
		if (!requestedCacheNames.isEmpty()) {
			throw new IllegalStateException("spring.cache.cache-names is not supported: " + requestedCacheNames
				 + ". Cache names come from CachePolicy only.");
		}
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		return builder -> builder
			 .cacheWriter(lockingCacheWriter(connectionFactory, properties))
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

	/**
	 * 같은 캐시의 적재를 Redis 락 하나로 줄 세워, MISS 가 몰려도 한 요청만 원본을 읽게 한다.
	 * <p>
	 * 락은 키가 아니라 캐시 이름 단위다(락 키 {@code <캐시 이름>~lock}, key-prefix 미적용).
	 * 기본 락에는 만료가 없어 보유자가 죽으면 그 캐시의 적재가 멈추므로 만료를 직접 건다.
	 * 적재가 락 만료보다 오래 걸리면 락이 풀려 다른 요청도 원본을 읽는다
	 * (spring-data-redis {@code DefaultRedisCacheWriter#doLock}, {@code RedisCacheWriter#lockingRedisCacheWriter}).
	 */
	private RedisCacheWriter lockingCacheWriter(
		 RedisConnectionFactory connectionFactory,
		 ServiceCacheProperties properties
	) {
		Duration lockRetryInterval = Duration.ofMillis(50);
		Duration lockTtl = Duration.ofSeconds(5);
		return RedisCacheWriter.lockingRedisCacheWriter(
			 connectionFactory,
			 lockRetryInterval,
			 RedisCacheWriter.TtlFunction.just(lockTtl),
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

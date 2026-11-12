package com.gmoon.ttlonly.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

import com.gmoon.cacheinvalidation.test.IntegrationTest;

@IntegrationTest
@DisplayName("TTL 만 쓰는 모듈의 설정")
class CacheConfigTest {

	private static final Duration DEFAULT_TTL = Duration.ofMinutes(7);
	private static final ServiceCacheProperties PROPERTIES = new ServiceCacheProperties(
		 new ServiceCacheProperties.Clear(1_000)
	);

	@Nested
	@DisplayName("만료에만 맡기므로")
	class WhenOnlyExpirationIsUsed {

		@Test
		@DisplayName("코어 모듈 없이 조립된다")
		void assemblesWithoutCoreModule() {
			assertThatExceptionOfType(ClassNotFoundException.class)
				 .as("무효화 파이프라인이 든 코어가 클래스패스에 있으면 쓰지 않는 빈과 설정을 다시 떠안게 된다")
				 .isThrownBy(() -> Class.forName("com.gmoon.cacheinvalidation.CachePolicy"));
		}
	}

	@Nested
	@DisplayName("기동하면")
	class WhenStarted {

		@Autowired CacheManager cacheManager;

		@Test
		@DisplayName("모든 정책 캐시가 만료를 가진다")
		void everyPolicyCacheExpires() {
			assertThat(CachePolicy.values())
				 .as("Redis 는 0 이하 TTL 을 만료 없음으로 다뤄 키가 영구히 남는다")
				 .allSatisfy(policy -> assertThat(registeredTtlOf(policy)).isPositive());
		}

		private Duration registeredTtlOf(CachePolicy policy) {
			RedisCache cache = (RedisCache)cacheManager.getCache(policy.cacheName());
			return cache.getCacheConfiguration().getTtlFunction().getTimeToLive("key", "value");
		}
	}

	@Nested
	@DisplayName("정책을 캐시 설정으로 바꿀 때")
	class WhenPolicyBecomesCacheConfiguration {

		@Nested
		@DisplayName("정책이 TTL 을 정하면")
		class WhenPolicyDeclaresTtl {

			@Test
			@DisplayName("Redis 기본 TTL 을 덮어쓴다")
			void overridesDefaultTtl() {
				RedisCacheConfiguration registered = registeredOver(defaultsWithTtl(), CachePolicy.USER);

				assertThat(ttlOf(registered))
					 .as("이 모듈의 stale 상한은 정책에 적은 TTL 이어야 한다")
					 .isEqualTo(CachePolicy.USER.ttl());
			}
		}

		@Nested
		@DisplayName("Redis 기본 설정이 키 접두사를 정하면")
		class WhenDefaultsGiveKeyPrefix {

			@Test
			@DisplayName("정책 캐시도 같은 접두사를 키 앞에 붙인다")
			void inheritsGivenPrefix() {
				RedisCacheConfiguration defaults = defaultsWithTtl().prefixCacheNameWith("svc:");

				RedisCacheConfiguration registered = registeredOver(defaults, CachePolicy.USER);

				assertThat(registered.getKeyPrefixFor(CachePolicy.Name.USER))
					 .as("정책 설정을 기본 설정과 따로 새로 만들면 spring.cache.redis.key-prefix 가 무시된다")
					 .startsWith("svc:");
			}
		}

		@Nested
		@DisplayName("Redis 기본 설정이 null 캐싱을 끄면")
		class WhenDefaultsDisableNullCaching {

			@Test
			@DisplayName("정책 캐시도 null 을 캐싱하지 않는다")
			void inheritsDisabledNullCaching() {
				RedisCacheConfiguration defaults = defaultsWithTtl().disableCachingNullValues();

				RedisCacheConfiguration registered = registeredOver(defaults, CachePolicy.USER);

				assertThat(registered.getAllowCacheNullValues())
					 .as("spring.cache.redis.cache-null-values 를 꺼도 정책 캐시가 따르지 않으면 설정이 거짓말을 한다")
					 .isFalse();
			}
		}

		private RedisCacheConfiguration registeredOver(RedisCacheConfiguration defaults, CachePolicy policy) {
			RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
			RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
				 .cacheDefaults(defaults);

			new CacheConfig()
				 .cacheManagerCustomizer(new CacheProperties(), connectionFactory, PROPERTIES)
				 .customize(builder);

			return builder.getCacheConfigurationFor(policy.cacheName())
				 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
		}

		private Duration ttlOf(RedisCacheConfiguration configuration) {
			return configuration.getTtlFunction().getTimeToLive("key", "value");
		}
	}

	@Nested
	@DisplayName("캐시 관리자를 만들 때")
	class WhenCacheManagerBuilt {

		@Nested
		@DisplayName("정책이 없는 캐시명을 요청하면")
		class WhenCacheHasNoPolicy {

			@Test
			@DisplayName("캐시를 돌려주지 않는다")
			void returnsNoCache() {
				assertThat(cacheManagerOver(mock(RedisConnectionFactory.class)).getCache("UNREGISTERED"))
					 .as("기본 설정으로 만들어 주면 TTL 도 값 타입도 없는 캐시가 조용히 생긴다")
					 .isNull();
			}
		}

		@Nested
		@DisplayName("캐시를 통째로 비우면")
		class WhenCacheCleared {

			@Test
			@DisplayName("KEYS 가 아니라 SCAN 으로 훑는다")
			void scansInsteadOfBlockingWithKeys() {
				RedisConnection connection = connectionYieldingNoKeys();

				cacheManagerOver(connectionFactoryOf(connection))
					 .getCache(CachePolicy.Name.USER)
					 .clear();

				then(connection).should(never().description(
					 "KEYS 는 키 개수만큼 레디스 단일 스레드를 점유해 운영 전체를 멈춘다")).keys(any());
				then(connection).should(description(
					 "RedisCacheWriter 를 빈으로만 두면 Boot 가 커넥션 팩토리로 직접 만들어 이 설정이 버려진다"))
					 .scan(any(ScanOptions.class));
			}
		}

		@Nested
		@DisplayName("spring.cache.cache-names 를 적으면")
		class WhenCacheNamesPropertyDeclared {

			@Test
			@DisplayName("정책과 같은 이름이어도 기동을 막는다")
			void rejectsEvenPolicyNames() {
				CacheProperties cacheProperties = new CacheProperties();
				cacheProperties.setCacheNames(List.of(CachePolicy.Name.USER));

				assertThatIllegalStateException()
					 .as("캐시 이름이 속성과 정책 두 곳에서 오면, 어느 쪽이 빠졌는지 따지는 검사가 따로 필요해진다")
					 .isThrownBy(() -> new CacheConfig().cacheManagerCustomizer(
						  cacheProperties,
						  mock(RedisConnectionFactory.class),
						  PROPERTIES
					 ))
					 .withMessageContaining("spring.cache.cache-names");
			}
		}

		private RedisCacheManager cacheManagerOver(RedisConnectionFactory connectionFactory) {
			RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
				 .cacheDefaults(defaultsWithTtl());

			new CacheConfig()
				 .cacheManagerCustomizer(new CacheProperties(), connectionFactory, PROPERTIES)
				 .customize(builder);

			RedisCacheManager cacheManager = builder.build();
			cacheManager.afterPropertiesSet();
			return cacheManager;
		}

		@SuppressWarnings("unchecked")
		private RedisConnection connectionYieldingNoKeys() {
			RedisConnection connection = mock(RedisConnection.class);
			given(connection.scan(any(ScanOptions.class))).willReturn(mock(Cursor.class));
			return connection;
		}

		private RedisConnectionFactory connectionFactoryOf(RedisConnection connection) {
			RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
			given(connectionFactory.getConnection()).willReturn(connection);
			return connectionFactory;
		}
	}

	@Nested
	@DisplayName("캐시 저장소가 실패하면")
	class WhenCacheStoreFails {

		private static final RuntimeException REDIS_DOWN = new IllegalStateException("connection refused");

		private final CacheErrorHandler handler = new CacheConfig().errorHandler();
		private final Cache cache = mock(Cache.class);

		@BeforeEach
		void setUp() {
			given(cache.getName()).willReturn(CachePolicy.Name.USER);
		}

		@Test
		@DisplayName("조회 실패를 삼켜 원본 조회로 넘어가게 한다")
		void swallowsGetSoOriginIsQueried() {
			assertThatNoException()
				 .as("조회 실패가 전파되면 캐시가 단일 장애점이 된다")
				 .isThrownBy(() -> handler.handleCacheGetError(REDIS_DOWN, cache, 1L));
		}

		@Test
		@DisplayName("적재 실패를 삼켜 응답에 영향을 주지 않는다")
		void swallowsPutWithoutAffectingResponse() {
			assertThatNoException()
				 .isThrownBy(() -> handler.handleCachePutError(REDIS_DOWN, cache, 1L, "value"));
		}

		@Test
		@DisplayName("무효화 실패를 삼킨다")
		void swallowsEvict() {
			assertThatNoException()
				 .as("무효화 유실은 stale 을 남기지만, 예외를 전파하면 요청 자체가 깨진다")
				 .isThrownBy(() -> handler.handleCacheEvictError(REDIS_DOWN, cache, 1L));
		}

		@Test
		@DisplayName("전체 삭제 실패를 삼킨다")
		void swallowsClear() {
			assertThatNoException()
				 .isThrownBy(() -> handler.handleCacheClearError(REDIS_DOWN, cache));
		}
	}

	private static RedisCacheConfiguration defaultsWithTtl() {
		return RedisCacheConfiguration.defaultCacheConfig().entryTtl(DEFAULT_TTL);
	}
}

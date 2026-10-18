package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.CacheEntry;
import com.gmoon.cacheinvalidation.CachePolicy;
import com.gmoon.cacheinvalidation.EntityChange;
import com.gmoon.cacheinvalidation.FailingCacheManager;
import com.gmoon.cacheinvalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.TestCachePolicy;
import com.gmoon.cacheinvalidation.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.serializer.SerializerFactory;

/**
 * 확장점은 {@code protected} 이므로 같은 패키지인 이 테스트가 스프링 컨텍스트 없이 직접 호출해 검증한다.
 * 다만 재정의한 전략이 실제 캐시 설정까지 닿는지는 {@code cacheManagerCustomizer} 를 거쳐 확인한다.
 */
@DisplayName("캐시 설정 확장점")
class AbstractRedisCacheConfigTest {

	private static final Duration FIXED_TTL = Duration.ofMinutes(42);
	private static final TtlResolver PASS_THROUGH = declaredTtl -> (key, value) -> declaredTtl;
	private static final double DEFAULT_JITTER_RATIO = 0.1;
	private static final ServiceCacheProperties PROPERTIES = new ServiceCacheProperties(null, null);

	@Nested
	@DisplayName("전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig();

		@Test
		@DisplayName("기본 만료 전략이 선언한 TTL 을 기준으로 흔들린 만료를 쓴다")
		void appliesJitteredTtlByDefault() {
			Duration declared = TestCachePolicy.USER.ttl();
			long bound = Math.max(1L, (long)(declared.toSeconds() * DEFAULT_JITTER_RATIO));

			assertThat(ttlOf(config))
				 .as("기본 만료 전략이 캐시 설정까지 닿지 않으면 동시 만료를 막지 못한다")
				 .isBetween(declared.minusSeconds(bound), declared.plusSeconds(bound));
		}

		@Test
		@DisplayName("기본 구현은 값을 JSON 으로 쓴다")
		void writesJsonByDefault() {
			assertThat(serializedValueOf(config))
				 .as("재정의 결과와 기본 결과가 같으면 아래 테스트가 아무것도 증명하지 못한다")
				 .isEqualTo("\"hello\"");
		}
	}

	@Nested
	@DisplayName("직렬화 전략을 재정의하면")
	class WhenSerializationOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig() {
			@Override
			protected SerializerFactory serializerFactory() {
				return new StringSerializerFactory();
			}
		};

		@Test
		@DisplayName("재정의한 구현이 반환된다")
		void returnsOverriddenStrategy() {
			assertThat(config.serializerFactory())
				 .as("상속으로 연 확장점이 기본 구현을 대체해야 한다")
				 .isInstanceOf(StringSerializerFactory.class);
		}

		@Test
		@DisplayName("캐시 설정이 재정의한 직렬화기로 값을 쓴다")
		void reachesCacheConfiguration() {
			assertThat(serializedValueOf(config))
				 .as("빈만 바뀌고 캐시 설정에 반영되지 않으면 확장점이 동작한 것이 아니다")
				 .isEqualTo("hello");
		}
	}

	@Nested
	@DisplayName("만료 전략을 재정의하면")
	class WhenExpirationOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig() {
			@Override
			protected TtlResolver ttlResolver(ServiceCacheProperties properties) {
				return declaredTtl -> (key, value) -> FIXED_TTL;
			}
		};

		@Test
		@DisplayName("재정의한 TTL 이 캐시 설정에 반영된다")
		void reachesCacheConfiguration() {
			assertThat(ttlOf(config))
				 .as("정책이 선언한 TTL 대신 재정의한 만료 전략이 이겨야 한다")
				 .isEqualTo(FIXED_TTL);
		}

		@Test
		@DisplayName("정책이 선언한 TTL 과 다른 값으로 검증한다")
		void differsFromPolicyDeclaredTtl() {
			assertThat(TestCachePolicy.USER.ttl())
				 .as("정책 TTL 과 재정의 TTL 이 같으면 위 테스트가 아무것도 증명하지 못한다")
				 .isNotEqualTo(FIXED_TTL);
		}
	}

	@Nested
	@DisplayName("무효화기를 조립하면")
	class WhenInvalidatorAssembled {

		@Test
		@DisplayName("캐시가 죽어도 예외를 호출자에게 돌려주지 않는다")
		void swallowsInvalidationFailure() {
			CacheInvalidator invalidator = new TestCacheConfig().cacheInvalidator(FailingCacheManager.of("redis down"));

			assertThatNoException()
				 .as("리스너가 각자 try/catch 하던 것을 한 곳으로 모았으므로, 배선이 빠지면 보호가 사라진다")
				 .isThrownBy(() -> invalidator.invalidate(EntityChange.of(new Object(), EntityChange.Type.DELETE)));
		}
	}

	@Nested
	@DisplayName("무효화 규칙을 선언하지 않으면")
	class WhenNoRuleDeclared {

		@Test
		@DisplayName("소유권을 따지지 않고 기동한다")
		void skipsOwnershipCheck() {
			AbstractRedisCacheConfig config = new AbstractRedisCacheConfig() {
				@Override
				protected List<CachePolicy> cachePolicies() {
					return List.of(TestCachePolicy.USER);
				}
			};

			assertThatNoException()
				 .as("무효화를 쓰지 않는 모듈은 TTL 이 유일한 수단이므로 주인을 물을 대상이 없다")
				 .isThrownBy(config::validateInvalidationOwnership);
		}
	}

	@Nested
	@DisplayName("무효화 규칙을 하나라도 선언하면")
	class WhenRuleDeclared {

		@Test
		@DisplayName("주인 없는 캐시를 기동에서 막는다")
		void rejectsPolicyWithoutOwner() {
			AbstractRedisCacheConfig config = new AbstractRedisCacheConfig() {
				@Override
				protected List<CachePolicy> cachePolicies() {
					return List.of(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY);
				}

				@Override
				protected List<InvalidationRule> invalidationRules() {
					return List.of(userCacheRule());
				}
			};

			assertThatIllegalStateException()
				 .as("규칙을 쓰기 시작하면 어떤 캐시를 지우고 어떤 캐시를 시간에 맡기는지 밝혀야 한다")
				 .isThrownBy(config::validateInvalidationOwnership)
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
		}
	}

	private String serializedValueOf(AbstractRedisCacheConfig config) {
		ByteBuffer written = configurationOf(config).getValueSerializationPair().write("hello");
		return StandardCharsets.UTF_8.decode(written).toString();
	}

	private Duration ttlOf(AbstractRedisCacheConfig config) {
		return configurationOf(config).getTtlFunction().getTimeToLive("key", "value");
	}

	private RedisCacheConfiguration configurationOf(AbstractRedisCacheConfig config) {
		RedisCacheManager.RedisCacheManagerBuilder builder =
			 RedisCacheManager.builder(mock(RedisConnectionFactory.class));

		config.cacheManagerCustomizer(
			 config.serializerFactory(),
			 config.ttlResolver(PROPERTIES),
			 new CacheProperties(),
			 mock(RedisConnectionFactory.class),
			 PROPERTIES
		).customize(builder);

		return builder.getCacheConfigurationFor(TestCachePolicy.Name.USER)
			 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
	}

	static class TestCacheConfig extends AbstractRedisCacheConfig {

		@Override
		protected List<CachePolicy> cachePolicies() {
			return List.of(TestCachePolicy.USER);
		}

		@Override
		protected List<InvalidationRule> invalidationRules() {
			return List.of(userCacheRule());
		}
	}

	private static InvalidationRule userCacheRule() {
		return new InvalidationRule() {
			@Override
			public boolean supports(EntityChange change) {
				return true;
			}

			@Override
			public Collection<CacheEntry> resolve(EntityChange change) {
				return List.of(CacheEntry.of(TestCachePolicy.USER, 1L));
			}

			@Override
			public Set<CachePolicy> owns() {
				return Set.of(TestCachePolicy.USER);
			}
		};
	}

	static class StringSerializerFactory implements SerializerFactory {

		@Override
		public RedisSerializer<?> valueSerializerFor(CachePolicy policy) {
			return RedisSerializer.string();
		}
	}

	@Nested
	@DisplayName("정책을 캐시 설정으로 바꿀 때")
	class CachePolicyCustomizing {

		@Nested
		@DisplayName("키 접두사를 쓰지 않기로 하면")
		class WhenKeyPrefixDisabled {

			@Test
			@DisplayName("캐시명을 키 앞에 붙이지 않는다")
			void writesKeyWithoutPrefix() {
				CacheProperties properties = new CacheProperties();
				properties.getRedis().setUseKeyPrefix(false);

				assertThat(registeredCacheOf(properties).usePrefix())
					 .as("getKeyPrefixFor 는 접두사를 쓴다면 무엇이 될지만 계산한다. 사용 여부는 usePrefix 가 쥔다")
					 .isFalse();
			}
		}

		@Nested
		@DisplayName("키 접두사를 지정하면")
		class WhenKeyPrefixGiven {

			@Test
			@DisplayName("지정한 값이 키 앞에 붙는다")
			void writesGivenPrefix() {
				CacheProperties properties = new CacheProperties();
				properties.getRedis().setKeyPrefix("svc:");

				assertThat(registeredCacheOf(properties).getKeyPrefixFor(TestCachePolicy.Name.USER))
					 .startsWith("svc:");
			}
		}

		@Nested
		@DisplayName("정책이 없는 캐시명을 요청하면")
		class WhenCacheHasNoPolicy {

			@Test
			@DisplayName("캐시를 돌려주지 않는다")
			void returnsNoCache() {
				assertThat(cacheManagerOf(mock(RedisConnectionFactory.class)).getCache("UNREGISTERED"))
					 .as("기본 설정으로 만들어 주면 정책 없는 캐시가 조용히 생기고 TTL 도 타입도 없이 산다")
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

				cacheManagerOf(connectionFactoryOf(connection))
					 .getCache(TestCachePolicy.Name.USER)
					 .clear();

				then(connection).should(never().description(
					 "KEYS 는 키 개수만큼 레디스 단일 스레드를 점유해 운영 전체를 멈춘다")).keys(any());
				then(connection).should(description(
					 "RedisCacheWriter 를 빈으로만 두면 Boot 가 커넥션 팩토리로 직접 만들어 이 설정이 버려진다"))
					 .scan(any(ScanOptions.class));
			}
		}

		private RedisCacheConfiguration registeredCacheOf(CacheProperties properties) {
			return builderWith(properties).getCacheConfigurationFor(TestCachePolicy.Name.USER)
				 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
		}

		private RedisCacheManager cacheManagerOf(RedisConnectionFactory connectionFactory) {
			RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory);

			config().cacheManagerCustomizer(
				 SerializerFactory.json(),
				 PASS_THROUGH,
				 new CacheProperties(),
				 connectionFactory,
				 PROPERTIES
			).customize(builder);

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

		private RedisCacheManager.RedisCacheManagerBuilder builderWith(CacheProperties properties) {
			RedisCacheManager.RedisCacheManagerBuilder builder =
				 RedisCacheManager.builder(mock(RedisConnectionFactory.class));

			config().cacheManagerCustomizer(
				 SerializerFactory.json(),
				 PASS_THROUGH,
				 properties,
				 mock(RedisConnectionFactory.class),
				 PROPERTIES
			).customize(builder);

			return builder;
		}

		private static AbstractRedisCacheConfig config() {
			return new AbstractRedisCacheConfig() {
				@Override
				protected List<CachePolicy> cachePolicies() {
					return List.of(TestCachePolicy.USER);
				}
			};
		}
	}
}

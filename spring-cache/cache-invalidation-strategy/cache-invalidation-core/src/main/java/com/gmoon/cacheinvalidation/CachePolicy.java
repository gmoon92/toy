package com.gmoon.cacheinvalidation;

import java.time.Duration;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 캐시 하나의 이름·수명·값 타입을 선언한다.
 * <p>
 * 도메인 모듈이 enum 으로 구현하고, 값은 {@link Spec} 에 담는다.
 * TTL 을 정하지 않은 정책은 Redis 캐시 기본 설정({@code spring.cache.redis.time-to-live})을 따른다.
 *
 * <pre>
 * public enum UserCachePolicy implements CachePolicy {
 *     USER(new Spec("USER", Duration.ofMinutes(10), CachedUser.class)),   // TTL 을 덮어쓴다
 *     PROFILE(new Spec("PROFILE", CachedProfile.class));                  // 기본 TTL 을 따른다
 * }
 * </pre>
 */
public interface CachePolicy {

	/**
	 * 같은 캐시 이름을 두 정책이 선언하면 기동을 막는다.
	 * <p>
	 * 중복을 허용하면 어느 쪽 TTL 과 값 타입이 이겼는지 런타임에만 드러난다.
	 */
	static Collection<CachePolicy> distinct(Collection<CachePolicy> policies) {
		Map<String, CachePolicy> byCacheName = new LinkedHashMap<>();
		for (CachePolicy policy : policies) {
			CachePolicy registered = byCacheName.get(policy.cacheName());
			Assert.isTrue(registered == null || registered == policy,
				 () -> "Duplicated cache name: " + policy.cacheName());
			byCacheName.put(policy.cacheName(), policy);
		}
		return List.copyOf(byCacheName.values());
	}

	Spec spec();

	default String cacheName() {
		return spec().cacheName();
	}

	/**
	 * 정책이 정한 TTL. 비어 있으면 Redis 캐시 기본 설정의 TTL 을 따른다.
	 */
	default Optional<Duration> ttl() {
		return Optional.ofNullable(spec().ttl());
	}

	default Class<?> valueType() {
		return spec().valueType();
	}

	record Spec(String cacheName, @Nullable Duration ttl, Class<?> valueType) {

		public Spec {
			Assert.hasText(cacheName, "cache name must not be empty");
			Assert.isTrue(
				 ttl == null || !(ttl.isNegative() || ttl.isZero()),
				 () -> "ttl must be positive when declared but was " + ttl
			);
			Assert.notNull(valueType, "value type must not be null");
		}

		/**
		 * TTL 을 정하지 않는다. Redis 캐시 기본 설정의 TTL 을 따른다.
		 */
		public Spec(String cacheName, Class<?> valueType) {
			this(cacheName, null, valueType);
		}
	}
}

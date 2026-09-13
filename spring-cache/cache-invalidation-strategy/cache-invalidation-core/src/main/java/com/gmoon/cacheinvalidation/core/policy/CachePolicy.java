package com.gmoon.cacheinvalidation.core.policy;

import java.time.Duration;

import org.springframework.util.Assert;

/**
 * 캐시 하나의 이름·수명·값 타입을 선언한다.
 * <p>
 * 도메인 모듈이 enum 으로 구현하고, 값은 {@link Spec} 에 담는다.
 *
 * <pre>
 * public enum UserCachePolicy implements CachePolicy {
 *     USER(new Spec("USER", Duration.ofMinutes(10), CachedUser.class));
 * }
 * </pre>
 */
public interface CachePolicy {

	Spec spec();

	default String cacheName() {
		return spec().cacheName();
	}

	default Duration ttl() {
		return spec().ttl();
	}

	default Class<?> valueType() {
		return spec().valueType();
	}

	record Spec(String cacheName, Duration ttl, Class<?> valueType) {

		public Spec {
			Assert.hasText(cacheName, "cache name must not be empty");
			Assert.notNull(ttl, "ttl must not be null");
			Assert.isTrue(!ttl.isNegative() && !ttl.isZero(), "ttl must be positive");
			Assert.notNull(valueType, "value type must not be null");
		}
	}
}

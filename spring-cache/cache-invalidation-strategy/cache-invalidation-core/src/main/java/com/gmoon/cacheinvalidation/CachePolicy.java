package com.gmoon.cacheinvalidation;

import java.time.Duration;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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

	/**
	 * 같은 캐시 이름을 두 정책이 선언하면 기동을 막는다.
	 * <p>
	 * 중복을 허용하면 어느 쪽 TTL 과 값 타입이 이겼는지 런타임에만 드러난다.
	 */
	static Collection<CachePolicy> distinct(Collection<CachePolicy> policies) {
		Map<String, CachePolicy> byCacheName = new LinkedHashMap<>();
		for (CachePolicy policy : policies) {
			verifyNameMatchesConstant(policy);

			CachePolicy registered = byCacheName.get(policy.cacheName());
			Assert.isTrue(registered == null || registered == policy,
				 () -> "Duplicated cache name: " + policy.cacheName());
			byCacheName.put(policy.cacheName(), policy);
		}
		return List.copyOf(byCacheName.values());
	}

	/**
	 * 캐시명은 두 곳에 적힌다 — enum 상수와, {@code @Cacheable} 에 넘길 문자열 상수다.
	 * 자바는 enum 상수를 애노테이션 인자로 쓰지 못하므로 둘을 합칠 수 없다.
	 * 대신 어긋나면 기동을 막아 드리프트를 런타임 밖으로 밀어낸다.
	 */
	private static void verifyNameMatchesConstant(CachePolicy policy) {
		if (!(policy instanceof Enum<?> constant)) {
			return;
		}
		Assert.isTrue(
			 constant.name().equals(policy.cacheName()),
			 () -> "Cache name must match the enum constant name. constant: " + constant.name()
				  + ", cache name: " + policy.cacheName()
		);
	}

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

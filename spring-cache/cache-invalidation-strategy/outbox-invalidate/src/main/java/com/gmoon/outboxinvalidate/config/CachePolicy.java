package com.gmoon.outboxinvalidate.config;

import java.time.Duration;

import com.gmoon.outboxinvalidate.user.UserView;

/**
 * 이 모듈이 쓰는 캐시를 전부 여기에 선언한다.
 * <p>
 * TTL 을 생략하면 10분이다. Spring Boot 표준 기본 TTL 은 {@code spring.cache.redis.time-to-live} 지만,
 * 이 모듈의 캐시는 모두 이 enum 이 TTL 을 정하므로 그 속성은 쓰지 않는다.
 * 쓰려면 TTL 을 생략한 정책이 {@code null} 을 갖게 하고,
 * {@link CacheConfig} 에서 그 정책에 {@code builder.cacheDefaults()} 의 TTL 을 대신 쓰면 된다.
 */
public enum CachePolicy {

	USER(Name.USER, UserView.class);

	private final String cacheName;
	private final Class<?> valueType;
	private final Duration ttl;

	CachePolicy(String cacheName, Class<?> valueType) {
		this(cacheName, valueType, Duration.ofMinutes(10));
	}

	CachePolicy(String cacheName, Class<?> valueType, Duration ttl) {
		this.cacheName = cacheName;
		this.valueType = valueType;
		this.ttl = ttl;
	}

	public String cacheName() {
		return cacheName;
	}

	public Class<?> valueType() {
		return valueType;
	}

	public Duration ttl() {
		return ttl;
	}

	/**
	 * 애노테이션 속성은 컴파일 상수만 받으므로 캐시 이름을 상수로 따로 둔다.
	 */
	public static final class Name {
		public static final String USER = "USER";

		private Name() {
		}
	}
}

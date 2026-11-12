package com.gmoon.ttlonly.config;

import java.time.Duration;

import com.gmoon.ttlonly.user.UserView;

/**
 * 이 모듈이 쓰는 캐시를 전부 여기에 선언한다.
 * <p>
 * 이 모듈은 TTL 로만 최신성을 회복하므로 TTL 이 곧 stale 상한이다.
 */
public enum CachePolicy {

	USER(Name.USER, UserView.class, Duration.ofSeconds(3));

	private final String cacheName;
	private final Class<?> valueType;
	private final Duration ttl;

	CachePolicy(String cacheName, Class<?> valueType) {
		this(cacheName, valueType, Duration.ofMinutes(5));
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

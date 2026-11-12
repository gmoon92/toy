package com.gmoon.ttlonly.config;

import java.time.Duration;
import java.util.Optional;

import org.springframework.lang.Nullable;

import com.gmoon.ttlonly.user.UserView;

/**
 * 이 모듈이 쓰는 캐시를 전부 여기에 선언한다.
 * <p>
 * 이 모듈은 TTL 로만 최신성을 회복하므로 TTL 이 곧 stale 상한이다.
 * TTL 을 정하지 않은 캐시는 {@code spring.cache.redis.time-to-live} 를 따른다.
 */
public enum CachePolicy {

	USER(Name.USER, Duration.ofSeconds(3), UserView.class);

	private final String cacheName;
	@Nullable
	private final Duration ttl;
	private final Class<?> valueType;

	CachePolicy(String cacheName, @Nullable Duration ttl, Class<?> valueType) {
		this.cacheName = cacheName;
		this.ttl = ttl;
		this.valueType = valueType;
	}

	/**
	 * TTL 을 정하지 않는다. {@code spring.cache.redis.time-to-live} 를 따른다.
	 */
	CachePolicy(String cacheName, Class<?> valueType) {
		this(cacheName, null, valueType);
	}

	public String cacheName() {
		return cacheName;
	}

	/**
	 * 정책이 정한 TTL. 비어 있으면 {@code spring.cache.redis.time-to-live} 를 따른다.
	 */
	public Optional<Duration> ttl() {
		return Optional.ofNullable(ttl);
	}

	public Class<?> valueType() {
		return valueType;
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

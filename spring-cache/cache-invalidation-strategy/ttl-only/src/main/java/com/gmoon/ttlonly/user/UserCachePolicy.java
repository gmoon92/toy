package com.gmoon.ttlonly.user;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;

public enum UserCachePolicy implements CachePolicy {

	USER(Name.USER, Duration.ofSeconds(3), CachedUser.class);

	private final String cacheName;
	private final Duration ttl;
	private final Class<?> valueType;

	UserCachePolicy(String cacheName, Duration ttl, Class<?> valueType) {
		this.cacheName = cacheName;
		this.ttl = ttl;
		this.valueType = valueType;
	}

	@Override
	public String cacheName() {
		return cacheName;
	}

	@Override
	public Duration ttl() {
		return ttl;
	}

	@Override
	public Class<?> valueType() {
		return valueType;
	}

	public static final class Name {
		public static final String USER = "USER";

		private Name() {
		}
	}
}

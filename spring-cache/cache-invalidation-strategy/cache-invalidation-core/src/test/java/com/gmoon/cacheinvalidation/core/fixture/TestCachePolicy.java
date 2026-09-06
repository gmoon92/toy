package com.gmoon.cacheinvalidation.core.fixture;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;

public enum TestCachePolicy implements CachePolicy {

	USER(Name.USER, Duration.ofMinutes(10), String.class),
	USER_SUMMARY(Name.USER_SUMMARY, Duration.ofMinutes(5), String.class);

	private final String cacheName;
	private final Duration ttl;
	private final Class<?> valueType;

	TestCachePolicy(String cacheName, Duration ttl, Class<?> valueType) {
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
		public static final String USER_SUMMARY = "USER_SUMMARY";

		private Name() {
		}
	}
}

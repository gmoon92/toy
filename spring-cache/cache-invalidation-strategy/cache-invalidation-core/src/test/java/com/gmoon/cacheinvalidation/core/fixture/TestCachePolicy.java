package com.gmoon.cacheinvalidation.core.fixture;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CacheSpec;

public enum TestCachePolicy implements CachePolicy {

	USER(CacheSpec.of(Name.USER, Duration.ofMinutes(10), String.class)),
	USER_SUMMARY(CacheSpec.of(Name.USER_SUMMARY, Duration.ofMinutes(5), String.class));

	private final CacheSpec spec;

	TestCachePolicy(CacheSpec spec) {
		this.spec = spec;
	}

	@Override
	public CacheSpec spec() {
		return spec;
	}

	public static final class Name {
		public static final String USER = "USER";
		public static final String USER_SUMMARY = "USER_SUMMARY";

		private Name() {
		}
	}
}

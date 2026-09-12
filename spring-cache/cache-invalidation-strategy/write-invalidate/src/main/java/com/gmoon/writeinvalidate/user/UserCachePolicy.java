package com.gmoon.writeinvalidate.user;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CacheSpec;

public enum UserCachePolicy implements CachePolicy {

	USER(CacheSpec.of(Name.USER, Duration.ofMinutes(10), CachedUser.class));

	private final CacheSpec spec;

	UserCachePolicy(CacheSpec spec) {
		this.spec = spec;
	}

	@Override
	public CacheSpec spec() {
		return spec;
	}

	public static final class Name {
		public static final String USER = "USER";

		private Name() {
		}
	}
}

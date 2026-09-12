package com.gmoon.ttlonly.user;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.CacheSpec;

public enum UserCachePolicy implements CachePolicy {

	USER(CacheSpec.of(Name.USER, Duration.ofSeconds(3), CachedUser.class).invalidatedByTtlOnly());

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

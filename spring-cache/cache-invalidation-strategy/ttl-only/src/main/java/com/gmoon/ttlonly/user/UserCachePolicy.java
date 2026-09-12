package com.gmoon.ttlonly.user;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;

public enum UserCachePolicy implements CachePolicy {

	USER(Spec.of(Name.USER, Duration.ofSeconds(3), CachedUser.class).invalidatedByTtlOnly());

	private final Spec spec;

	UserCachePolicy(Spec spec) {
		this.spec = spec;
	}

	@Override
	public Spec spec() {
		return spec;
	}

	public static final class Name {
		public static final String USER = "USER";

		private Name() {
		}
	}
}

package com.gmoon.writeinvalidate.user;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

public enum UserCachePolicy implements CachePolicy {

	USER(new Spec(Name.USER, Duration.ofMinutes(10), CachedUser.class));

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

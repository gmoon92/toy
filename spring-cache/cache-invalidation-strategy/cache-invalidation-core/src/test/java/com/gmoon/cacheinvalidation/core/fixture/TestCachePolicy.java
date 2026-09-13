package com.gmoon.cacheinvalidation.core.fixture;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.policy.CachePolicy;

public enum TestCachePolicy implements CachePolicy {

	USER(new Spec(Name.USER, Duration.ofMinutes(10), String.class)),
	USER_SUMMARY(new Spec(Name.USER_SUMMARY, Duration.ofMinutes(5), String.class));

	private final Spec spec;

	TestCachePolicy(Spec spec) {
		this.spec = spec;
	}

	@Override
	public Spec spec() {
		return spec;
	}

	public static final class Name {
		public static final String USER = "USER";
		public static final String USER_SUMMARY = "USER_SUMMARY";

		private Name() {
		}
	}
}

package com.gmoon.writeinvalidate.article;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;

public enum ArticleCachePolicy implements CachePolicy {

	ARTICLE(Spec.of(Name.ARTICLE, Duration.ofMinutes(10), CachedArticle.class).invalidatedByTtlOnly());

	private final Spec spec;

	ArticleCachePolicy(Spec spec) {
		this.spec = spec;
	}

	@Override
	public Spec spec() {
		return spec;
	}

	public static final class Name {
		public static final String ARTICLE = "ARTICLE";

		private Name() {
		}
	}
}

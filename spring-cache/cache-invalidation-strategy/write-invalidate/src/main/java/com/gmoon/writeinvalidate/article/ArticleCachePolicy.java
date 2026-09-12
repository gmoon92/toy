package com.gmoon.writeinvalidate.article;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CacheSpec;

public enum ArticleCachePolicy implements CachePolicy {

	ARTICLE(CacheSpec.of(Name.ARTICLE, Duration.ofMinutes(10), CachedArticle.class).invalidatedByTtlOnly());

	private final CacheSpec spec;

	ArticleCachePolicy(CacheSpec spec) {
		this.spec = spec;
	}

	@Override
	public CacheSpec spec() {
		return spec;
	}

	public static final class Name {
		public static final String ARTICLE = "ARTICLE";

		private Name() {
		}
	}
}

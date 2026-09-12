package com.gmoon.writeinvalidate.article;

import java.time.Duration;

import com.gmoon.cacheinvalidation.core.cache.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.InvalidationMode;

public enum ArticleCachePolicy implements CachePolicy {

	ARTICLE(Name.ARTICLE, Duration.ofMinutes(10), CachedArticle.class);

	private final String cacheName;
	private final Duration ttl;
	private final Class<?> valueType;

	ArticleCachePolicy(String cacheName, Duration ttl, Class<?> valueType) {
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

	@Override
	public InvalidationMode invalidationMode() {
		return InvalidationMode.TTL_ONLY;
	}

	public static final class Name {
		public static final String ARTICLE = "ARTICLE";

		private Name() {
		}
	}
}

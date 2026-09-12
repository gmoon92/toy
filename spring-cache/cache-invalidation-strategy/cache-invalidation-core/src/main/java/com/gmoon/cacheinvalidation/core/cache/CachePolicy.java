package com.gmoon.cacheinvalidation.core.cache;

import java.time.Duration;

public interface CachePolicy {

	CacheSpec spec();

	default String cacheName() {
		return spec().cacheName();
	}

	default Duration ttl() {
		return spec().ttl();
	}

	default Class<?> valueType() {
		return spec().valueType();
	}

	default InvalidationMode invalidationMode() {
		return spec().invalidationMode();
	}
}

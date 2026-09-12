package com.gmoon.cacheinvalidation.core.cache.policy;

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

	default InvalidationOwner invalidationOwner() {
		return spec().invalidationOwner();
	}
}

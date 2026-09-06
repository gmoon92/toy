package com.gmoon.cacheinvalidation.core.cache;

import java.time.Duration;

public interface CachePolicy {

	String cacheName();

	Duration ttl();

	Class<?> valueType();
}

package com.gmoon.cacheinvalidation.core.cache.expiration;

import java.time.Duration;

import org.springframework.data.redis.cache.RedisCacheWriter;

public interface CacheExpiration {

	RedisCacheWriter.TtlFunction ttlFunctionOf(Duration baseTtl);
}

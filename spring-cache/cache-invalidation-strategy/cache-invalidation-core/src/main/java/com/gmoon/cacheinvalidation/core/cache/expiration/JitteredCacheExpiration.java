package com.gmoon.cacheinvalidation.core.cache.expiration;

import java.time.Duration;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class JitteredCacheExpiration implements CacheExpiration {

	private final JitteredTtl jitteredTtl;
	private final Duration notFoundTtl;

	public JitteredCacheExpiration(double jitterRatio, Duration notFoundTtl) {
		this(new JitteredTtl(jitterRatio), notFoundTtl);
	}

	public JitteredCacheExpiration(JitteredTtl jitteredTtl, Duration notFoundTtl) {
		this.jitteredTtl = jitteredTtl;
		this.notFoundTtl = notFoundTtl;
	}

	@Override
	public RedisCacheWriter.TtlFunction ttlFunctionOf(Duration baseTtl) {
		return (key, value) -> value instanceof NullValue
			 ? jitteredTtl.apply(notFoundTtl)
			 : jitteredTtl.apply(baseTtl);
	}
}

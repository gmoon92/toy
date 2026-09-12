package com.gmoon.cacheinvalidation.core.cache.expiration;

import java.time.Duration;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 선언된 TTL 에 흔들림을 얹고, 비어 있는 값은 더 짧게 만료시킨다.
 * <p>
 * 같은 시각에 적재된 키가 한꺼번에 만료되어 DB 로 몰리는 것을 막는다.
 */
public class JitteredTtlResolver implements TtlResolver {

	private final JitteredTtl jitteredTtl;
	private final Duration notFoundTtl;

	public JitteredTtlResolver(double jitterRatio, Duration notFoundTtl) {
		this(new JitteredTtl(jitterRatio), notFoundTtl);
	}

	public JitteredTtlResolver(JitteredTtl jitteredTtl, Duration notFoundTtl) {
		this.jitteredTtl = jitteredTtl;
		this.notFoundTtl = notFoundTtl;
	}

	@Override
	public RedisCacheWriter.TtlFunction resolveFrom(Duration declaredTtl) {
		return (key, value) -> value instanceof NullValue
			 ? jitteredTtl.apply(notFoundTtl)
			 : jitteredTtl.apply(declaredTtl);
	}
}

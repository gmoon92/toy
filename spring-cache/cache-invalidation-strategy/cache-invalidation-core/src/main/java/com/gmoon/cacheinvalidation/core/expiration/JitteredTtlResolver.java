package com.gmoon.cacheinvalidation.core.expiration;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongUnaryOperator;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * 선언된 TTL 에 흔들림을 얹고, 비어 있는 값은 더 짧게 만료시킨다.
 * <p>
 * 같은 시각에 적재된 키가 한꺼번에 만료되어 DB 로 몰리는 것을 막는다.
 */
public class JitteredTtlResolver implements TtlResolver {

	private static final long MINIMUM_JITTER_SECONDS = 1L;

	private final double jitterRatio;
	private final Duration notFoundTtl;
	private final LongUnaryOperator offsetGenerator;

	public JitteredTtlResolver(double jitterRatio, Duration notFoundTtl) {
		this(jitterRatio, notFoundTtl, bound -> ThreadLocalRandom.current().nextLong(-bound, bound + 1));
	}

	JitteredTtlResolver(double jitterRatio, Duration notFoundTtl, LongUnaryOperator offsetGenerator) {
		this.jitterRatio = jitterRatio;
		this.notFoundTtl = notFoundTtl;
		this.offsetGenerator = offsetGenerator;
	}

	@Override
	public RedisCacheWriter.TtlFunction resolveFrom(Duration declaredTtl) {
		return (key, value) -> value instanceof NullValue
			 ? jittered(notFoundTtl)
			 : jittered(declaredTtl);
	}

	private Duration jittered(Duration baseTtl) {
		return baseTtl.plusSeconds(offsetGenerator.applyAsLong(boundOf(baseTtl)));
	}

	private long boundOf(Duration baseTtl) {
		long jitterSeconds = (long)(baseTtl.toSeconds() * jitterRatio);
		return Math.max(MINIMUM_JITTER_SECONDS, jitterSeconds);
	}
}

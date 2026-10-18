package com.gmoon.cacheinvalidation.expiration;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongUnaryOperator;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.util.Assert;

/**
 * 선언된 TTL 에 흔들림을 얹고, 비어 있는 값은 더 짧게 만료시킨다.
 * <p>
 * 같은 시각에 적재된 키가 한꺼번에 만료되어 DB 로 몰리는 것을 막는다.
 * <p>
 * 흔들림이 기준 TTL 을 깎아 0 이하로 내려가면 안 된다.
 * Spring Data Redis 는 0 이하 TTL 을 "만료 없음" 으로 해석해 키가 영구히 남는다.
 */
class JitteredTtlResolver implements TtlResolver {

	private static final long MINIMUM_JITTER_SECONDS = 1L;
	private static final Duration MINIMUM_TTL = Duration.ofSeconds(1);

	private final double jitterRatio;
	private final Duration notFoundTtl;
	private final LongUnaryOperator offsetGenerator;

	JitteredTtlResolver(double jitterRatio, Duration notFoundTtl) {
		this(jitterRatio, notFoundTtl, bound -> ThreadLocalRandom.current().nextLong(-bound, bound + 1));
	}

	JitteredTtlResolver(double jitterRatio, Duration notFoundTtl, LongUnaryOperator offsetGenerator) {
		Assert.isTrue(
			 jitterRatio >= 0 && jitterRatio < 1,
			 "jitter ratio must be between 0 (inclusive) and 1 (exclusive) but was " + jitterRatio
		);
		Assert.notNull(notFoundTtl, "not found ttl must not be null");

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
		return atLeastMinimum(baseTtl.plusSeconds(offsetGenerator.applyAsLong(boundOf(baseTtl))));
	}

	private static Duration atLeastMinimum(Duration ttl) {
		return ttl.compareTo(MINIMUM_TTL) < 0 ? MINIMUM_TTL : ttl;
	}

	private long boundOf(Duration baseTtl) {
		long jitterSeconds = (long)(baseTtl.toSeconds() * jitterRatio);
		return Math.max(MINIMUM_JITTER_SECONDS, jitterSeconds);
	}
}

package com.gmoon.cacheinvalidation.core.cache;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongUnaryOperator;

public final class JitteredTtl {

	private static final long MINIMUM_JITTER_SECONDS = 1L;

	private final double jitterRatio;
	private final LongUnaryOperator offsetGenerator;

	public JitteredTtl(double jitterRatio) {
		this(jitterRatio, bound -> ThreadLocalRandom.current().nextLong(-bound, bound + 1));
	}

	JitteredTtl(double jitterRatio, LongUnaryOperator offsetGenerator) {
		this.jitterRatio = jitterRatio;
		this.offsetGenerator = offsetGenerator;
	}

	public Duration apply(Duration baseTtl) {
		long bound = boundOf(baseTtl);
		return baseTtl.plusSeconds(offsetGenerator.applyAsLong(bound));
	}

	private long boundOf(Duration baseTtl) {
		long jitterSeconds = (long)(baseTtl.toSeconds() * jitterRatio);
		return Math.max(MINIMUM_JITTER_SECONDS, jitterSeconds);
	}
}

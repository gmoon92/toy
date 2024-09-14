package com.gmoon.timesorteduniqueidentifier.idgenerator.tsid;

import com.gmoon.timesorteduniqueidentifier.idgenerator.base.BitUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * <pre>
 *     128
 * {timestamp}{randomness}
 *   48bits      80bits
 * </pre>
 */
@Slf4j
public class TsidGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public long generate() {
		long timestamp = systemTimeMillis();
		long random = nextValue();
		log.info("Generated timestamp: {}, random: {}", timestamp, random);

		return BigInteger.valueOf(timestamp)
			 .shiftLeft(Bits.RANDOMNESS)
			 .or(BigInteger.valueOf(random))
			 .longValue();
	}

	private long systemTimeMillis() {
		return BitUtils.masking(
			 System.currentTimeMillis(),
			 Bits.TIMESTAMP
		);
	}

	private long nextValue() {
		return BitUtils.masking(SECURE_RANDOM.nextLong(), Bits.RANDOMNESS);
	}

	static class Bits {

		public static final int TIMESTAMP = 48;
		public static final int RANDOMNESS = 80;
	}
}

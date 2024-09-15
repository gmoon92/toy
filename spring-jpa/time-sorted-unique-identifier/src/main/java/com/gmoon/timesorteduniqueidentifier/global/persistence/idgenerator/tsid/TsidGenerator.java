package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.tsid;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import static com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.base.BitUtils.masking;

/**
 * <pre>
 *     128
 * {timestamp}{randomness}
 *   48bits      80bits
 * </pre>
 */
@Slf4j
public class TsidGenerator {

	private final ReentrantLock lock = new ReentrantLock();
	private final Random random = new SecureRandom();

	private long timestamp = -1L;
	private long count;

	public BigInteger generate() {
		try {
			lock.lock();
			long timestamp = systemTimeMillis();
			long randomValue = random.nextLong();

			log.trace("Generated timestamp: {}, random: {}, count: {}", timestamp, randomValue, count);
			return BigInteger.valueOf(masking(timestamp, Bits.TIMESTAMP))
				 .shiftLeft(Bits.RANDOMNESS)
				 .or(BigInteger.valueOf(masking(randomValue, Bits.RANDOM)).shiftLeft(Bits.COUNT))
				 .or(BigInteger.valueOf(count));
		} finally {
			lock.unlock();
		}
	}

	private long systemTimeMillis() {
		long timestamp = System.currentTimeMillis();
		boolean conflictTime = timestamp <= this.timestamp;
		if (conflictTime) {
			count++;

			long carry = count >>> Bits.COUNT;
			boolean overflow = carry > 0;
			if (overflow) {
				log.trace("overflow count: {}", count);
				timestamp = this.timestamp + carry; // increment time
				resetCount();
			}
			count = (int) masking(count, Bits.COUNT);
		} else {
			resetCount();
		}

		this.timestamp = timestamp;
		return timestamp;
	}

	private void resetCount() {
		count = 0;
	}

	static class Bits {

		public static final int TIMESTAMP = 48;
		public static final int RANDOMNESS = 80;
		public static final int RANDOM = 63; // max = 2^6 -1 = long type bits(64bits, 2^6) -1
		public static final int COUNT = RANDOMNESS - RANDOM;
	}
}

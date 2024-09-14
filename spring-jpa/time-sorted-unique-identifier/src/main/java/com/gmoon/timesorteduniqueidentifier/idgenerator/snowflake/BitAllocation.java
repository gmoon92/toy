package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import java.util.stream.Stream;

/**
 * <pre>
 * {sign bit}{timestamp}{instance}{sequence}
 *    1bits     41bits    10bits    12bits
 * </pre>
 */
public enum BitAllocation {

	SIGN_BIT(1, 1),
	TIMESTAMP(2, 41),
	DATA_CENTER(3, 5),
	WORKER_ID(4, 5),
	SEQUENCE(5, 12);

	public final int position;
	public final long bits;
	public final long bitMask;

	BitAllocation(int position, long bits) {
		this.position = position;
		this.bits = bits;
		this.bitMask = ~(-1L << bits);
	}

	public long shift() {
		return Stream.of(values())
			 .filter(allocation -> allocation.position > position)
			 .mapToLong(bit -> bit.bits)
			 .sum();
	}

	public long masking(long value) {
		return value & bitMask;
	}

	public long extract(long snowflakeId) {
		return (snowflakeId >> shift()) & bitMask;
	}
}

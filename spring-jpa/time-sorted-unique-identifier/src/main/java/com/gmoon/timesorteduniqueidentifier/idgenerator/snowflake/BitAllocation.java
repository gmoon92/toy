package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * <pre>
 * {sign bit}{timestamp}{instance}{sequence}
 *    1bits     41bits    10bits    12bits
 * </pre>
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PRIVATE)
public enum BitAllocation {

	SIGN_BIT(1, 1),
	TIMESTAMP(2, 41),
	DATA_CENTER(3, 5),
	WORKER_ID(4, 5),
	SEQUENCE(5, 12);

	public final int position;
	public final long bitLength;

	public long shiftLeft() {
		return Stream.of(values())
			 .filter(allocation -> allocation.position > position)
			 .mapToLong(BitAllocation::getBitLength)
			 .sum();
	}

	public long shiftRight() {
		return Stream.of(values())
			 .filter(allocation -> allocation.position < position)
			 .mapToLong(BitAllocation::getBitLength)
			 .sum();
	}

	public long getBitMask() {
		return ~(-1L << bitLength);
	}

	public long extract(long snowflakeId) {
		return (snowflakeId >> shiftLeft()) & getBitMask();
	}
}

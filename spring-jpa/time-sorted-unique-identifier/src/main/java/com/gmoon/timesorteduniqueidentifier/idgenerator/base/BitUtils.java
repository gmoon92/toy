package com.gmoon.timesorteduniqueidentifier.idgenerator.base;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class BitUtils {

	public static long masking(long value, long bits) {
		return value & getBitMask(bits);
	}

	public static long extract(long value, long position, long bits) {
		long bitMask = getBitMask(bits);
		return (value >> position) & bitMask;
	}

	private static long getBitMask(long bits) {
		return ~(-1L << bits);
	}
}

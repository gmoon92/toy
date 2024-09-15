package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.base;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;

@UtilityClass
public final class BitUtils {

	public static long masking(long value, long bits) {
		return value & getBitMask(bits);
	}

	public static long extract(long value, long bits, long position) {
		long bitMask = getBitMask(bits);
		return (value >> position) & bitMask;
	}

	public static long extract(BigInteger value, int bits, int position) {
		long bitMask = getBitMask(bits);
		return value.shiftRight(position).longValue() & bitMask;
	}

	public static long extract(String hexString, int bits, int position) {
		long bitMask = getBitMask(bits);
		return new BigInteger(hexString, 16).shiftRight(position).longValue() & bitMask;
	}

	private static long getBitMask(long bits) {
		return ~(-1L << bits);
	}
}

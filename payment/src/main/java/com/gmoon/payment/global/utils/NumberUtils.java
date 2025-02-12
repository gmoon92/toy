package com.gmoon.payment.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtils {

	public static String toString(Long l) {
		if (l == null) {
			return null;
		}

		return Long.toString(l);
	}

	public static String toString(Integer i) {
		if (i == null) {
			return null;
		}

		return Integer.toString(i);
	}

	public static BigDecimal toBigDecimal(Long l) {
		return BigDecimal.valueOf(defaultValue(l));
	}

	public static BigDecimal toBigDecimal(Integer i) {
		return BigDecimal.valueOf(defaultValue(i));
	}

	public static BigDecimal toBigDecimal(Double d) {
		return BigDecimal.valueOf(defaultValue(d));
	}

	public static double defaultValue(Double d) {
		if (d == null) {
			return 0;
		}
		return d;
	}

	public static long defaultValue(Integer i) {
		if (i == null) {
			return 0;
		}
		return i;
	}

	public static long defaultValue(Long l) {
		if (l == null) {
			return 0;
		}
		return l;
	}

}

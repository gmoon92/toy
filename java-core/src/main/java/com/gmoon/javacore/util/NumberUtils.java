package com.gmoon.javacore.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NumberUtils {

	public static <T extends Number> int toInt(T number) {
		if (number == null) {
			return 0;
		}

		try {
			long l = number.longValue();
			return Math.toIntExact(l);
		} catch (Exception e) {
			log.debug("value is overflows.");
			return Integer.MAX_VALUE;
		}
	}

	public static int positiveNumberOrZero(int a) {
		return Math.max(a, 0);
	}

	public static long positiveNumberOrZero(long a) {
		return Math.max(a, 0);
	}

	public static int defaultInteger(Integer i) {
		return defaultInteger(i, 0);
	}

	public static int defaultInteger(Integer value, Integer defaultValue) {
		if (value == null) {
			return defaultValue;
		}

		return value;
	}
}

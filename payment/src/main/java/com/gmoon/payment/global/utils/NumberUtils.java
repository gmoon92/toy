package com.gmoon.payment.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}

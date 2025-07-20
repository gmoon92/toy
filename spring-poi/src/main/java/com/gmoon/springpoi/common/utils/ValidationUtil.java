package com.gmoon.springpoi.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtil {

	public static boolean isRange(int targetLength, int min, int max) {
		if (targetLength == 0)
			return true;

		return min <= targetLength && targetLength <= max;
	}
}

package com.gmoon.javacore.util;

import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.tsid.TsidCreator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @apiNote Utility class for generating formatted unique strings
 * based on TSID (Time-Sorted Unique Identifier).
 *
 * <p>Example: {@code generate(4, 4, "-")} → XXXX-XXXX-XXXX-XXXX</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsidUtils {

	/**
	 * Generates a unique, formatted string using TSID and random padding.
	 *
	 * @param partDigit the number of characters in each segment (e.g., 4 → XXXX)
	 * @param repeat the number of segments (e.g., 4 → XXXX-XXXX-XXXX-XXXX)
	 * @param delimiter the delimiter used to separate segments (e.g., "-" → XXXX-XXXX-XXXX-XXXX)
	 * @return a formatted, uppercase base62 string with random padding
	 * @throws IllegalArgumentException if partDigit or repeat is less than 1,
	 *                                  if delimiter is null or empty,
	 *                                  or if the total length (partDigit × repeat) is less than 13
	 * @apiNote Generates a unique string based on TSID, with padding and a customizable delimiter for better formatting.
	 */
	public static String generate(int partDigit, int repeat, String delimiter) {
		if (partDigit < 1) {
			throw new IllegalArgumentException("partDigit must be at least 1, but got " + partDigit);
		}

		if (repeat < 1) {
			throw new IllegalArgumentException("repeat must be at least 1, but got " + repeat);
		}

		int size = partDigit * repeat;
		if (size < 13) {
			throw new IllegalArgumentException("Total length (partDigit × repeat) must be at least 13 to fully contain a TSID string. Current total length: " + size);
		}

		Tsid tsid4096 = TsidCreator.getTsid4096();
		String base62 = tsid4096.format("%z"); // Base62로 인코딩된 11~13자리 문자열

		String padded = StringUtils.rightPad(
			 base62,
			 size,
			 StringUtils.randomAlphabetic(size)
		);

		// 각 파트를 지정된 길이마다 나누기
		String[] parts = padded.split("(?<=\\G.{" + partDigit + "})");
		return StringUtils.join(parts, delimiter)
			 .toUpperCase();
	}
}

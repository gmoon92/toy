package com.gmoon.javacore.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

	public static String randomAlphabetic(int length) {
		return RandomStringUtils.randomAlphabetic(length);
	}

	public static boolean isEmpty(String str) {
		return org.apache.commons.lang3.StringUtils.isEmpty(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean isEmptyAny(String... args) {
		for (String str : args) {
			if (isEmpty(str)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isBlank(String str) {
		return org.apache.commons.lang3.StringUtils.isBlank(str);
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static boolean isBlankAny(String... args) {
		for (String str : args) {
			if (isBlank(str)) {
				return true;
			}
		}

		return false;
	}

	public static String defaultIfBlank(String str, String defaultString) {
		return isBlank(str) ? defaultString : str;
	}

	public static String defaultString(String str) {
		return defaultString(str, "");
	}

	public static String defaultString(String str, String defaultString) {
		return org.apache.commons.lang3.StringUtils.defaultString(str, defaultString);
	}

	public static String replace(String str, String searchString, String replacement) {
		return org.apache.commons.lang3.StringUtils.replace(str, searchString, replacement);
	}

	public static String remove(String str, String... removes) {
		for (String removeStr : removes) {
			str = org.apache.commons.lang3.StringUtils.remove(str, removeStr);
		}

		return str;
	}

	public static String substring(String str, int start, int end) {
		return org.apache.commons.lang3.StringUtils.substring(str, start, end);
	}

	public static int indexOf(String str, String searchStr) {
		return org.apache.commons.lang3.StringUtils.indexOf(str, searchStr);
	}

	public static int indexOf(String str, String searchStr, int startPos) {
		return org.apache.commons.lang3.StringUtils.indexOf(str, searchStr, startPos);
	}

	public static String trim(String str) {
		return org.apache.commons.lang3.StringUtils.trimToEmpty(str);
	}

	public static String lowerCase(String str) {
		return org.apache.commons.lang3.StringUtils.lowerCase(str);
	}

	public static String upperCase(String str) {
		return org.apache.commons.lang3.StringUtils.upperCase(str);
	}

	public static boolean contains(String str, String searchString) {
		return org.apache.commons.lang3.StringUtils.contains(str, searchString);
	}

	public static boolean containsAny(String str, String... searchStrings) {
		for (String search : searchStrings) {
			if (contains(str, search))
				return true;
		}

		return false;
	}

	public static String[] split(String str, String delimiter) {
		return org.apache.commons.lang3.StringUtils.split(str, delimiter);
	}

	public static String[] split(String str, String delimiter, int max) {
		return org.apache.commons.lang3.StringUtils.split(str, delimiter, max);
	}

	public static String join(Object[] array, String delimiter) {
		return org.apache.commons.lang3.StringUtils.join(array, delimiter);
	}

	public static boolean equals(CharSequence str1, CharSequence str2) {
		return org.apache.commons.lang3.StringUtils.equals(str1, str2);
	}

	public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
		return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(str1, str2);
	}

	public static byte[] getBytes(String str, String charset) {
		try {
			return org.apache.commons.lang3.StringUtils.getBytes(str, charset);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

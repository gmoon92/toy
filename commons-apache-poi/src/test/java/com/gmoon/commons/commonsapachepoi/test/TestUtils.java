package com.gmoon.commons.commonsapachepoi.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class TestUtils {

	public static String randomString(String prefix, int length) {
		return prefix + randomString(length);
	}

	public static String randomString(int length) {
		return RandomStringUtils.secure()
			 .nextAlphanumeric(length);
	}

	public static boolean randomBoolean() {
		return RandomUtils.secure()
			 .randomBoolean();
	}
}

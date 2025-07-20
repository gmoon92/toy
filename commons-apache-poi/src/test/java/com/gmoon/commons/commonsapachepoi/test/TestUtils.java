package com.gmoon.commons.commonsapachepoi.test;

import java.util.function.Supplier;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public final class TestUtils {

	private static final Supplier<RandomUtils> RANDOM = RandomUtils::secure;
	private static final Supplier<RandomStringUtils> RANDOM_STRING = RandomStringUtils::secure;

	public static String randomString(String prefix, int length) {
		return prefix + randomString(length);
	}

	public static String randomString(int length) {
		return RANDOM_STRING.get().nextAlphanumeric(length);
	}

	public static boolean randomBoolean() {
		return RANDOM.get().randomBoolean();
	}

	public static int getRandomInteger(int startInclusive, int endExclusive) {
		return RANDOM.get().randomInt(startInclusive, endExclusive);
	}

	@SafeVarargs
	public static <T> T pickRandom(T... t) {
		int random = getRandomInteger(0, t.length);
		return t[random];
	}
}

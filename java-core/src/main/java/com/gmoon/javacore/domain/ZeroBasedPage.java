package com.gmoon.javacore.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeroBasedPage<T extends Number> {

	private final int value;

	private ZeroBasedPage(T page) {
		if (page == null) {
			value = 0;
		} else {
			value = toInt(page);
		}
	}

	public static <T extends Number> int from(T page) {
		return new ZeroBasedPage<>(page).value;
	}

	private int toInt(T page) {
		try {
			long value = page.longValue();
			log.trace("page: {}, value: {}", page, value);
			return Math.max(Math.toIntExact(value) - 1, 0);
		} catch (ArithmeticException e) {
			log.debug("value is overflows.");
			return Integer.MAX_VALUE;
		}
	}
}

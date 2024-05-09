package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeroBasedPage<T extends Number> {

	private final int value;

	private ZeroBasedPage(T page) {
		if (page == null) {
			value = 0;
		} else {
			value = positiveNumber(toInt(page) - 1);
		}
	}

	public static <T extends Number> int from(T page) {
		return new ZeroBasedPage<>(page).value;
	}
}

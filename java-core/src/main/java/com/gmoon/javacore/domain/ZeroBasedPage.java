package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeroBasedPage {

	private final int value;

	private ZeroBasedPage(Number page) {
		if (page == null) {
			value = 0;
		} else {
			value = getValueOrMax(page);
		}
	}

	private int getValueOrMax(Number page) {
		boolean overflows = page.longValue() > Integer.MAX_VALUE;
		if (overflows) {
			return Integer.MAX_VALUE;
		}
		
		return positiveNumberOrZero(toInt(page) - 1);
	}

	public static <T extends Number> int from(T page) {
		return new ZeroBasedPage(page).value;
	}
}

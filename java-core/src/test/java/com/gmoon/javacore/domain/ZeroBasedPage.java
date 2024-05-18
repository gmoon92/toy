package com.gmoon.javacore.domain;

import static com.gmoon.javacore.util.NumberUtils.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
public class ZeroBasedPage {

	private static final ZeroBasedPage ZERO = new ZeroBasedPage(0);

	private final int value;

	private <T extends Number> ZeroBasedPage(T page) {
		value = positiveNumberOrZero(toInt(page));
	}

	public static ZeroBasedPage zero() {
		return ZERO;
	}

	public static <T extends Number> ZeroBasedPage from(T page) {
		return new ZeroBasedPage(page);
	}

	public static <T extends Number> ZeroBasedPage adjust(T page) {
		if (page == null) {
			return ZERO;
		}

		boolean overflows = page.longValue() > Integer.MAX_VALUE;
		if (overflows) {
			return new ZeroBasedPage(Integer.MAX_VALUE);
		}

		return new ZeroBasedPage(toInt(page) - 1);
	}
}

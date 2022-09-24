package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void randomAlphabetic() {
		int length = 10;
		String random = StringUtils.randomAlphabetic(length);

		assertThat(random)
			.hasSize(length)
			.isNotEqualTo(StringUtils.randomAlphabetic(length));
	}
}

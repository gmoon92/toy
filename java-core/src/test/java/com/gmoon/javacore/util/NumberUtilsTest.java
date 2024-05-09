package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumberUtilsTest {

	@DisplayName("양의 정수 반환")
	@Test
	void positiveNumberOrZero() {
		assertThat(NumberUtils.positiveNumberOrZero(-1)).isZero();
		assertThat(NumberUtils.positiveNumberOrZero(0)).isZero();
		assertThat(NumberUtils.positiveNumberOrZero(1)).isOne();
	}
}

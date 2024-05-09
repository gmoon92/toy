package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumberUtilsTest {

	@DisplayName("양의 정수 반환")
	@Test
	void positiveNumber() {
		assertThat(NumberUtils.positiveNumber(-1)).isZero();
		assertThat(NumberUtils.positiveNumber(0)).isZero();
		assertThat(NumberUtils.positiveNumber(1)).isOne();
	}
}

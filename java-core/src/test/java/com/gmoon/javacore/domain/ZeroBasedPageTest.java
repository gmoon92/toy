package com.gmoon.javacore.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ZeroBasedPageTest {

	@DisplayName("클라이언트 페이지 1부터 시작, 페이지 처리시 0 부터 시작")
	@Test
	void nullSafe() {
		assertThat(ZeroBasedPage.from(null)).isZero();
		assertThat(ZeroBasedPage.from(0)).isZero();
		assertThat(ZeroBasedPage.from(1)).isZero();

		assertThat(ZeroBasedPage.from(2)).isOne();
	}

	@DisplayName("Integer 값 범위를 초과할 경우 MAX 값 반환")
	@Test
	void integerValueOverflows() {
		assertThat(ZeroBasedPage.from(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE - 1);

		assertThat(ZeroBasedPage.from(Double.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
		assertThat(ZeroBasedPage.from(Long.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
	}
}

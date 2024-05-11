package com.gmoon.javacore.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ZeroBasedPageTest {

	@DisplayName("클라이언트 페이지 1부터 시작, 페이지 처리시 0 부터 시작")
	@Test
	void nullSafe() {
		assertThat(ZeroBasedPage.adjust(null)).isEqualTo(ZeroBasedPage.zero());
		assertThat(ZeroBasedPage.adjust(0)).isEqualTo(ZeroBasedPage.zero());
		assertThat(ZeroBasedPage.adjust(1)).isEqualTo(ZeroBasedPage.zero());

		assertThat(ZeroBasedPage.adjust(2)).isEqualTo(ZeroBasedPage.from(1));
	}

	@DisplayName("Integer 값 범위를 초과할 경우 MAX 값 반환")
	@Test
	void adjust() {
		assertThat(ZeroBasedPage.adjust(Integer.MAX_VALUE)).isEqualTo(ZeroBasedPage.from(Integer.MAX_VALUE - 1));

		assertThat(ZeroBasedPage.adjust(Double.MAX_VALUE)).isEqualTo(ZeroBasedPage.from(Integer.MAX_VALUE));
		assertThat(ZeroBasedPage.adjust(Long.MAX_VALUE)).isEqualTo(ZeroBasedPage.from(Integer.MAX_VALUE));
	}
}

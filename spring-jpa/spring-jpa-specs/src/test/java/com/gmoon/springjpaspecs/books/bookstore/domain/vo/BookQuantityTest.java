package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BookQuantityTest {

	@DisplayName("메뉴 상품 수량을 생성할 수 있다.")
	@Test
	void create() {
		BookQuantity actual = new BookQuantity(0);

		assertThat(actual).isEqualTo(new BookQuantity(0));
		assertThat(actual.hashCode() == new BookQuantity(0).hashCode())
			.isTrue();
	}

	@DisplayName("책 수량은 0개 이상이어야 한다.")
	@ParameterizedTest(name = "{displayName}[{index}] - {arguments}")
	@ValueSource(ints = -1)
	void error1(int quantity) {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new BookQuantity(quantity));
	}
}

package com.gmoon.springjpaspecs.books.book.domain.vo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BookPriceTest {

	@Test
	void create() {
		assertThatCode(() -> new BookPrice(BigDecimal.ZERO))
			.doesNotThrowAnyException();
	}

	@ParameterizedTest
	@ValueSource(strings = "-1")
	@NullSource
	void error(BigDecimal price) {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new BookPrice(price));
	}
}

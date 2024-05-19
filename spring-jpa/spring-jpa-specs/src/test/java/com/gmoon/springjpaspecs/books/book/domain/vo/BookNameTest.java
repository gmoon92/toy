package com.gmoon.springjpaspecs.books.book.domain.vo;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BookNameTest {

	@DisplayName("이름을 생성한다.")
	@Test
	void create() {
		assertThatCode(() -> new BookName("gmoon"))
			 .doesNotThrowAnyException();
	}

	@DisplayName("이름은 비어있을 수 없다.")
	@ParameterizedTest
	@NullAndEmptySource
	void error(String name) {
		assertThatExceptionOfType(IllegalArgumentException.class)
			 .isThrownBy(() -> new BookName(name));
	}
}

package com.gmoon.springjpaspecs.books.book.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IStandardBookNumberTest {

	@Test
	void create() {
		IStandardBookNumber actual = new IStandardBookNumber();

		assertThat(actual)
			.isNotNull()
			.isNotEqualTo(new IStandardBookNumber());
	}
}
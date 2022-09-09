package com.gmoon.junit5.assertj;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.junit5.assertj.model.Shape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssertJTest {

	@DisplayName("usingRecursiveComparison " +
		"내부적으로 리플렉션을 사용하여 필드 데이터 검증")
	@Test
	void usingRecursiveComparison() {
		Shape vo1 = new Shape(20);

		assertThat(vo1)
			.usingRecursiveComparison()
			.isEqualTo(new Shape(20));

		// failure
		// assertThat(vo1).isEqualTo(new Shape(20));
	}
}

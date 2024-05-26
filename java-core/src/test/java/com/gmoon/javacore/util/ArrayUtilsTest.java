package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArrayUtilsTest {

	@Test
	void isEmpty() {
		int[] array = new int[] {};

		assertThat(ArrayUtils.isEmpty(array)).isTrue();
	}
}

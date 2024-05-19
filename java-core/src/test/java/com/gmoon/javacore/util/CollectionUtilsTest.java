package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

	@Test
	void testMinSize() {
		// given
		List<String> names = Arrays.asList("gmoon", "guest");
		List<Integer> ages = Arrays.asList(10, 20, 30);

		// when
		int minSize = CollectionUtils.minSize(names, ages);

		// then
		assertThat(minSize)
			 .isEqualTo(2);
	}
}

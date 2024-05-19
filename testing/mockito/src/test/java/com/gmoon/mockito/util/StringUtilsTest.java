package com.gmoon.mockito.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class StringUtilsTest {

	@DisplayName("utils class mocking")
	@Test
	void testMockingStaticMethods() {
		try (MockedStatic<StringUtils> utils = Mockito.mockStatic(StringUtils.class)) {
			String name = "gmoon";

			utils.when(() -> StringUtils.isEmpty(name))
				 .thenReturn(true);

			assertThat(StringUtils.isEmpty("gmoon2")).isFalse();
			assertThat(StringUtils.isEmpty(name)).isTrue();
		}
	}

	static final class StringUtils {
		private StringUtils() {
		}

		static boolean isEmpty(String str) {
			return str == null || str.length() == 0;
		}
	}
}

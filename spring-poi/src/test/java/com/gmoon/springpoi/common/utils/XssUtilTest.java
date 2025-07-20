package com.gmoon.springpoi.common.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class XssUtilTest {

	@Test
	void getCleanHTML() {
		assertThat(XssUtil.getCleanHTML("<script>alert('test');</script>")).isBlank();
		assertThat(XssUtil.getCleanHTML("test")).isEqualTo("test");
	}
}

package com.gmoon.springpoi.common.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import com.gmoon.springpoi.test.TestUtils;

class DigestUtilTest {

	@RepeatedTest(100)
	void sha256() {
		int length = TestUtils.randomInteger(0, 10_000);
		String random = TestUtils.randomString(length);
		Assertions.assertThat(DigestUtil.sha256(random))
			 .matches("[a-f0-9]{64}")
			 .hasSize(64);
	}
}

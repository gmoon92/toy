package com.gmoon.junit5.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssumptionsTest {

	@DisplayName("assumeTrue 통과가 되지 않으면 뒤 검증은 무시한다.")
	@Test
	void testAssumeTrue() {
		assumeEnvironmentLocalTest("api");

		assertThat("test").isEqualTo("!!!");
	}

	private static void assumeEnvironmentLocalTest(String env) {
		Assumptions.assumeTrue("local".equals(env), "로컬 환경에서만 테스트");
	}
}

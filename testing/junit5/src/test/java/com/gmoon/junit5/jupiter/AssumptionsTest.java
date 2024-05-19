package com.gmoon.junit5.jupiter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssumptionsTest {

	@BeforeEach
	void setUp() {
		assumeEnvironmentLocalTest("web");
	}

	@DisplayName("@BeforeEach 에서 설정된 assume 가정문이 통과되지 않는다면," +
		 "해당 테스트 disabled 처리")
	@Test
	void testAssumeWithBeforeEach() {
		assertThat(true).isFalse();
	}

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

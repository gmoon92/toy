package com.gmoon.springquartzcluster.service_check;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gmoon.springquartzcluster.test.TestPropertiesConfig;

import lombok.RequiredArgsConstructor;

@Disabled("github action 로 인한 테스트 비활성화")
@TestPropertiesConfig(DiskSpaceCheckExecutor.class)
@RequiredArgsConstructor
class DiskSpaceCheckExecutorTest {
	final DiskSpaceCheckExecutor executor;

	@Test
	@DisplayName("디스크 사용량을 측정한다.")
	void testCheck() {
		executor.check();
	}
}

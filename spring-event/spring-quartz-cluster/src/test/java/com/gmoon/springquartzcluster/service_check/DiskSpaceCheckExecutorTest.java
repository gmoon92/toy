package com.gmoon.springquartzcluster.service_check;

import com.gmoon.springquartzcluster.test.TestPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
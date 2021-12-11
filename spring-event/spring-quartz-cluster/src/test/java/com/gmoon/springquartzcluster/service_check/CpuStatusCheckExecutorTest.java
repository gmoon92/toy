package com.gmoon.springquartzcluster.service_check;

import com.gmoon.springquartzcluster.test.TestPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@TestPropertiesConfig(classes = CpuStatusCheckExecutor.class)
@RequiredArgsConstructor
class CpuStatusCheckExecutorTest {
  final CpuStatusCheckExecutor executor;

  @Test
  @DisplayName("JVM 프로세스에서 사용하고 있는 CPU 사용량을 측정한다.")
  void testCheck() {
    executor.check();
  }
}
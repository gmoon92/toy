package com.gmoon.springquartzcluster.service_check;

import com.gmoon.springquartzcluster.test.TestPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestPropertiesConfig(JvmMemoryUsageCheckExecutor.class)
@RequiredArgsConstructor
class JvmMemoryUsageCheckExecutorTest {
  final JvmMemoryUsageCheckExecutor executor;

  @Test
  @DisplayName("JVM 메모리 사용량을 측정한다.")
  void testCheck() {
    executor.check();
  }
}
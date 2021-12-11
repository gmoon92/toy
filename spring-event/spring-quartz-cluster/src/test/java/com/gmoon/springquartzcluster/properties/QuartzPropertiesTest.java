package com.gmoon.springquartzcluster.properties;

import com.gmoon.springquartzcluster.test.TestPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertiesConfig
@RequiredArgsConstructor
class QuartzPropertiesTest {
  final QuartzProperties properties;

  @Test
  @DisplayName("Quartz Job 프로퍼티 활성화 여부를 검증한다.")
  void testPropertiesEnabled() {
    // when then
    verifyPropertyEnabled(properties.isJvmMemoryUsageEnabled());
    verifyPropertyEnabled(properties.isDiskUsageEnabled());
    verifyPropertyEnabled(properties.isCpuUsageEnabled());
  }

  private void verifyPropertyEnabled(boolean diskUsageEnabled) {
    assertThat(diskUsageEnabled).isTrue();
  }

  @Test
  @DisplayName("서비스 체크에 사용할 임계값을 검증한다.")
  void testPropertiesValue() {
    // given
    int jvmMemoryThresholdPercentage = properties.getJvmMemoryThresholdPercentage();
    int diskThresholdPercentage = properties.getDiskThresholdPercentage();
    int cpuThresholdPercentage = properties.getCpuThresholdPercentage();

    // when then
    verifyThresholdPercentage(jvmMemoryThresholdPercentage, 98);
    verifyThresholdPercentage(diskThresholdPercentage, 98);
    verifyThresholdPercentage(cpuThresholdPercentage, 90);
  }

  private void verifyThresholdPercentage(int percentage, int expected) {
    assertThat(percentage).isEqualTo(expected);
  }
}
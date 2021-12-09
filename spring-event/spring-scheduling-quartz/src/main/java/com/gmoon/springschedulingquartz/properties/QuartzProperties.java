package com.gmoon.springschedulingquartz.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "service-check")
public class QuartzProperties {
  private final JvmMemoryUsage jvmMemoryUsage;
  private final DiskUsage diskSpace;
  private final CpuUsage cpuUsage;

  @RequiredArgsConstructor
  private static class CpuUsage {
    private final boolean enabled;
    private final int thresholdPercentage;
  }

  @RequiredArgsConstructor
  private static class JvmMemoryUsage {
    private final boolean enabled;
    private final int thresholdPercentage;
  }

  @RequiredArgsConstructor
  private static class DiskUsage {
    private final boolean enabled;
    private final int thresholdPercentage;
  }

  public boolean isCpuUsageEnabled() {
    return cpuUsage.enabled;
  }

  public int getCpuThresholdPercentage() {
    return cpuUsage.thresholdPercentage;
  }

  public boolean isJvmMemoryUsageEnabled() {
    return jvmMemoryUsage.enabled;
  }

  public int getJvmMemoryThresholdPercentage() {
    return jvmMemoryUsage.thresholdPercentage;
  }

  public boolean isDiskUsageEnabled() {
    return diskSpace.enabled;
  }

  public int getDiskThresholdPercentage() {
    return diskSpace.thresholdPercentage;
  }
}

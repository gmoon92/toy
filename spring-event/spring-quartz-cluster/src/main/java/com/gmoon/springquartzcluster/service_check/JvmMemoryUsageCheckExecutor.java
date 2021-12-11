package com.gmoon.springquartzcluster.service_check;

import com.gmoon.springquartzcluster.exception.JvmMemoryUsageExceedsException;
import com.gmoon.springquartzcluster.properties.QuartzProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JvmMemoryUsageCheckExecutor implements ServiceStatusChecker {
  private final QuartzProperties properties;

  @Override
  public void check() {
    if (!properties.isJvmMemoryUsageEnabled()) {
      return;
    }

    long memory = Runtime.getRuntime().totalMemory();
    long available = Runtime.getRuntime().freeMemory();
    long used = memory - available;

    int thresholdPercentage = properties.getJvmMemoryThresholdPercentage();
    float usage = getPercent(memory, used);
    log.info("thresholdPercentage: {}, usage: {}, memory: {}, available: {}", thresholdPercentage, usage, memory, available);
    if (usage > thresholdPercentage) {
      throw new JvmMemoryUsageExceedsException();
    }
  }

  private float getPercent(long total, long part) {
    if (total == 0) {
      return 0;
    }
    return part * 100.0f / total;
  }
}

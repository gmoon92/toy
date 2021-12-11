package com.gmoon.springquartzcluster.service_check;

import com.gmoon.springquartzcluster.exception.DiskUsageExceedsException;
import com.gmoon.springquartzcluster.properties.QuartzProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiskSpaceCheckExecutor implements ServiceStatusChecker {
  private final QuartzProperties properties;

  @Override
  public void check() {
    if (!properties.isDiskUsageEnabled()) {
      return;
    }

    File dir = new File("/");
    long total = dir.getTotalSpace();
    long available = dir.getUsableSpace();
    long used = total - available;

    int thresholdPercentage = properties.getDiskThresholdPercentage();
    float usage = getPercent(total, used);
    log.info("thresholdPercentage: {}, usage: {}, path: {}, total: {}, available: {}", thresholdPercentage, usage, dir.getAbsolutePath(), total, available);
    if (usage > thresholdPercentage) {
      throw new DiskUsageExceedsException();
    }
  }

  private float getPercent(long total, long part) {
    if (total == 0) {
      return 0;
    }
    return part * 100.0f / total;
  }
}

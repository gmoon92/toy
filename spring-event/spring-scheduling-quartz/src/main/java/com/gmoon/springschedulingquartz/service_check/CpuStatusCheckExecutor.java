package com.gmoon.springschedulingquartz.service_check;

import com.gmoon.springschedulingquartz.exception.CpuUsageExceedsException;
import com.gmoon.springschedulingquartz.exception.NotFoundInvokeMethodException;
import com.gmoon.springschedulingquartz.exception.ReflectionException;
import com.gmoon.springschedulingquartz.properties.QuartzProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class CpuStatusCheckExecutor implements ServiceStatusChecker {
  private static final String CPU_MEASURE_METHOD_NAME = "getProcessCpuTime";
  private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getOperatingSystemMXBean();
  private static final RuntimeMXBean RUNTIME_BEAN = ManagementFactory.getRuntimeMXBean();

  private final QuartzProperties properties;

  @Override
  public void check() {
    if (!properties.isCpuUsageEnabled()) {
      return;
    }

    int thresholdPercentage = properties.getCpuThresholdPercentage();
    int usage = getCpuUsage();
    log.info("thresholdPercentage: {}, usage: {}", thresholdPercentage, usage);
    if (usage > thresholdPercentage) {
      throw new CpuUsageExceedsException();
    }
  }

  private int getCpuUsage() {
    long beforeProcessTime = getJVMProcessUsingCpuTime();
    long beforeUptime = RUNTIME_BEAN.getUptime();
    long cpuCount = OS_BEAN.getAvailableProcessors();

    for (int i = 0; i < 1_000_000; ++i) {
      cpuCount = OS_BEAN.getAvailableProcessors();
    }

    long afterProcessTime = getJVMProcessUsingCpuTime();
    long afterUptime = RUNTIME_BEAN.getUptime();

    long processTimeLeft = afterProcessTime - beforeProcessTime;
    long uptimeLeft = afterUptime - beforeUptime;
    float usage = processTimeLeft / ( uptimeLeft * 10_000f * cpuCount );
    return (int) Math.min(99f, usage);
  }

  private long getJVMProcessUsingCpuTime() {
    return Arrays.stream(OS_BEAN.getClass().getDeclaredMethods())
            .map(this::getAccessibleMethod)
            .filter(this::isInvokeMethod)
            .findFirst()
            .map(this::getProcessCpuTime)
            .orElseThrow(NotFoundInvokeMethodException::new);
  }

  private boolean isInvokeMethod(Method method) {
    return StringUtils.equals(CPU_MEASURE_METHOD_NAME, method.getName())
            && Modifier.isPublic(method.getModifiers());
  }

  private Method getAccessibleMethod(Method method) {
    method.setAccessible(true);
    return method;
  }

  private long getProcessCpuTime(Method method) {
    try {
      return (long) method.invoke(OS_BEAN);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionException(e);
    }
  }
}

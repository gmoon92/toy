package com.gmoon.springschedulingquartz.service_check;

import com.gmoon.springschedulingquartz.config.PropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SpringJUnitConfig(value = { ServiceStatusCheckService.class,
        CpuStatusCheckExecutor.class, DiskSpaceCheckExecutor.class, JvmMemoryUsageCheckExecutor.class,
        PropertiesConfig.class })
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ServiceStatusCheckServiceTest {
  final ServiceStatusCheckService service;

  @MockBean CpuStatusCheckExecutor cpuStatusCheckExecutor;
  @MockBean DiskSpaceCheckExecutor diskSpaceCheckExecutor;
  @MockBean JvmMemoryUsageCheckExecutor jvmMemoryUsageCheckExecutor;

  @Test
  @DisplayName("ServiceStatusChecker 인터페이스를 구현한 빈들이 서비스 상태를 검사한다.")
  void testCheckAll() {
    // when
    service.checkAll();

    // then
    verifyCheckMethodCall(cpuStatusCheckExecutor);
    verifyCheckMethodCall(diskSpaceCheckExecutor);
    verifyCheckMethodCall(jvmMemoryUsageCheckExecutor);
  }

  private void verifyCheckMethodCall(ServiceStatusChecker serviceStatusChecker) {
    then(serviceStatusChecker).should(times(1)).check();
  }
}
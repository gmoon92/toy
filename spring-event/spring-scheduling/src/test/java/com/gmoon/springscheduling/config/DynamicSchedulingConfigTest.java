package com.gmoon.springscheduling.config;

import com.gmoon.springscheduling.jobs.PhoneAlarmJobs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

@Slf4j
@SpringBootTest
@EnabledIf(expression = "#{'${schedule.type}' == 'dynamic'}", loadContext = true)
class DynamicSchedulingConfigTest {

  @SpyBean
  PhoneAlarmJobs alarmJobs;

  @Test
  @DisplayName("스케쥴러는 트리거에 지정된 시간만큼 더해서 job을 수행한다.")
  void dynamicDelayTimeTrigger() throws InterruptedException {
    // given when
    Thread.sleep(3000);

    // then
    then(alarmJobs)
            .should(atLeast(2))
            .plusOneSecondsDelay();
  }
}
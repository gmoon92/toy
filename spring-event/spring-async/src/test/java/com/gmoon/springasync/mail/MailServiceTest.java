package com.gmoon.springasync.mail;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
class MailServiceTest {

  @Autowired
  MailService mailService;

  @Mock
  MailSender mailSender;

  @Autowired
  BeanFactory beanFactory;

  @Test
  @DisplayName("현재 프로젝트에서 @EnableAsync에서 주입하고 있는 Task Executor 를 알아보자")
  void taskExecutorName() {
    // given
    Class<TaskExecutor> asyncExecutor = TaskExecutor.class;

    // when
    Object executor = beanFactory.getBean(asyncExecutor);

    // then
    assertThat(executor)
            .isNotInstanceOf(SimpleAsyncTaskExecutor.class)
            .isInstanceOf(ThreadPoolTaskExecutor.class);
  }

  @Test
  @DisplayName("비동기시 초대 메일을 보낸 서버 주소를 반환 받는다.")
  void sendInviteMailFromServerWillReturn() throws InterruptedException {
    // given
    String publicUrl = "https://gmoon92.github.io";

    // when
    Future<String> future = mailService.sendInviteMailFromServerWillReturn(publicUrl);

    // then
    while (future.isDone()) {
      Assumptions.assumingThat(future.isDone(),
              () -> assertThat(future.get()).isEqualTo(publicUrl));
      log.info("Alive thread...");
      wait(1000);
    }
  }
}
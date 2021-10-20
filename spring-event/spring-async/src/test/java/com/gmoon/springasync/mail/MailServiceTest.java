package com.gmoon.springasync.mail;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
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
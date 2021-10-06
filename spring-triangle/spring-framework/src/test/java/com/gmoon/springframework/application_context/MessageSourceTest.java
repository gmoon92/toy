package com.gmoon.springframework.application_context;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(MessageSourceTest.CustomMessageSourceConfig.class)
public class MessageSourceTest {

  @Autowired
  MessageSource messageSource;

  @Test
  void messageSource() {
    // given
    String messageCode = "CODE0000";
    String[] params = { "gmoon" };

    // when
    String language = messageSource.getMessage(messageCode, params, Locale.KOREA);

    // then
    assertAll(() -> assertThat(messageSource).isExactlyInstanceOf(ResourceBundleMessageSource.class),
            () -> assertThat(language)
                    .isEqualTo("안녕 gmoon"));
  }

  @TestConfiguration
  static class CustomMessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages/message");
      messageSource.setDefaultEncoding("UTF-8");
      messageSource.setCacheSeconds(5);
      return messageSource;
    }


  }

}

package com.gmoon.springsecurity.account;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class PasswordEncoderTest {

  @Autowired
  PasswordEncoder passwordEncoder;

  @Test
  void encode() {
    String rawPassword = "123";
    String password = passwordEncoder.encode(rawPassword);
    String password2 = passwordEncoder.encode(rawPassword);

    log.info("password: {}", password);
    log.info("password2: {}", password2);

    assertThat(passwordEncoder.matches(rawPassword, password))
            .isTrue();
    assertThat(passwordEncoder.matches(rawPassword, password2))
            .isTrue();
  }
}

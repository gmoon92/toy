package com.gmoon.springlogging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
public class SpringLoggingApplication implements ApplicationRunner {

  public static void main(String[] args) {
    SpringApplication.run(SpringLoggingApplication.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    while (true) {
      log.info("message: {}", LocalDateTime.now());
      Thread.sleep(1_000);
    }
  }
}

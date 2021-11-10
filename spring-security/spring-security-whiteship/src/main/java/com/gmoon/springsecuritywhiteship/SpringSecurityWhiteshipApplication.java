package com.gmoon.springsecuritywhiteship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpringSecurityWhiteshipApplication {

  public static void main(String[] args) {
    SpringApplication.run(com.gmoon.springsecuritywhiteship.SpringSecurityWhiteshipApplication.class, args);
  }

}

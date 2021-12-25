package com.gmoon.springlogging;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SpringLoggingApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SpringLoggingApplication.class, args);
		while (true) {
			log.info("message: {}", LocalDateTime.now());
			Thread.sleep(1_000);
		}
	}
}

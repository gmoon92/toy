package com.gmoon.springsecuritywhiteship.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static void logging(String message) {
		log.info("Message : {}", message);
		log.info("Thread : {}", Thread.currentThread().getName());
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.info("Principal : {}", principal);
	}
}

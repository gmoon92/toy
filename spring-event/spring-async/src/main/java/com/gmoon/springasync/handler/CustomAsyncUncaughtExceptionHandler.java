package com.gmoon.springasync.handler;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		log.error("Exception message - " + ex.getMessage(), ex);
		log.error("Method name: {}", method.getName());
		for (Object param : params) {
			log.error("param: {}", param);
		}
	}
}

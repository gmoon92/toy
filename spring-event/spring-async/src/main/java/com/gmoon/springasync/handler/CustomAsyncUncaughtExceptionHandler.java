package com.gmoon.springasync.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class CustomAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    log.info("Exception message - {}", ex.getMessage());
    log.info("Method name: {}", method.getName());
    for (Object param : params) {
      log.info("param: {}", param);
    }
  }
}

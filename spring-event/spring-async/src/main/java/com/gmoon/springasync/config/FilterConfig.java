package com.gmoon.springasync.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
  private final DispatcherServlet dispatcherServlet;

  @PostConstruct
  public void init() {
    dispatcherServlet.setThreadContextInheritable(true);
  }
}

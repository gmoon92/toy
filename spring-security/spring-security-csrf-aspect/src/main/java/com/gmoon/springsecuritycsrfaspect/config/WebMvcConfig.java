package com.gmoon.springsecuritycsrfaspect.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("login");
    registry.addViewController("/login").setViewName("login");
    registry.addViewController("/error/403").setViewName("error/403");
    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
  }
}

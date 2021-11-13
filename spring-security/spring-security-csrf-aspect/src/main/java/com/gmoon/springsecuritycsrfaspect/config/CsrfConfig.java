package com.gmoon.springsecuritycsrfaspect.config;

import com.gmoon.springsecuritycsrfaspect.csrf.CsrfTokenAspect;
import com.gmoon.springsecuritycsrfaspect.csrf.CsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CsrfConfig {

  @Bean
  public CsrfTokenRepository csrfTokenRepository() {
    return new CsrfTokenRepository();
  }

  @Bean
  public CsrfTokenAspect csrfTokenAspect(HttpServletRequest request, CsrfTokenRepository csrfTokenRepository) {
    return new CsrfTokenAspect(request, csrfTokenRepository);
  }
}

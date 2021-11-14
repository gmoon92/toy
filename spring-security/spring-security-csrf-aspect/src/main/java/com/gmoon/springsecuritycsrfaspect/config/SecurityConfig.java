package com.gmoon.springsecuritycsrfaspect.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
            .headers()
              .frameOptions().sameOrigin().and()
              .csrf().disable()
              .cors().disable()
            .authorizeRequests()
              .mvcMatchers("**/user/**").hasRole("ADMIN")
              .anyRequest()
              .permitAll().and()
            .exceptionHandling()
              .accessDeniedHandler(accessDeniedHandler()).and()
            .formLogin()
              .loginPage("/login")
              .defaultSuccessUrl("/user/info")
              .permitAll();
  }

  private AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      SecurityContext context = SecurityContextHolder.getContext();
      Authentication authentication = context.getAuthentication();
      UserDetails user = (UserDetails) authentication.getPrincipal();

      String username = user.getUsername();
      String method = request.getMethod();
      String url = request.getRequestURI();
      String errorMessage = String.format("%s denied to access (%s) %s.", username, method, url);
      log.error(errorMessage, accessDeniedException);

      response.sendRedirect("/error/403");
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}

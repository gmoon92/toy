package com.gmoon.springsecuritycsrfaspect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

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
              .mvcMatchers("/", "/login")
              .permitAll()
              .anyRequest()
              .authenticated().and()
            .formLogin()
              .loginPage("/")
            .permitAll();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}

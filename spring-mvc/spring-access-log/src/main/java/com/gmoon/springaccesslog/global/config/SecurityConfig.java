package com.gmoon.springaccesslog.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true, securedEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			 .csrf(AbstractHttpConfigurer::disable)
			 .headers(config -> config.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .authorizeHttpRequests(
				  auth -> auth
					   .requestMatchers("/login", "/user/login").permitAll()
					   .anyRequest().authenticated()
			 )
			 .csrf(AbstractHttpConfigurer::disable)
			 .formLogin(
				  formLogin -> formLogin
					   .usernameParameter("username")
					   .passwordParameter("password")
					   .loginProcessingUrl("/user/login")
					   .defaultSuccessUrl("/user")
			 )
			 .build();
	}
}

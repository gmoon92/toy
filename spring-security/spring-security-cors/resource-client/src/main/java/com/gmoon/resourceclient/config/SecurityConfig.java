package com.gmoon.resourceclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.gmoon.resourceclient.security.OAuthenticationFilter;
import com.gmoon.resourceclient.security.OAuthenticationProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			 .exceptionHandling(this::exceptionHandling)
			 .headers(config -> config.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .authorizeHttpRequests(authorization -> authorization.anyRequest().authenticated())
			 .authenticationProvider(authenticationProvider())
			 .addFilter(new OAuthenticationFilter(authenticationManager(), authenticationFailureHandler()))
			 .formLogin(Customizer.withDefaults())
			 .build();
	}

	private AuthenticationManager authenticationManager() {
		return authentication -> authentication;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new OAuthenticationProvider();
	}

	private void exceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> configurer) {
		configurer.accessDeniedHandler((request, response, accessDeniedException) -> {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication authentication = context.getAuthentication();
			UserDetails user = (UserDetails)authentication.getPrincipal();

			String username = user.getUsername();
			String method = request.getMethod();
			String url = request.getRequestURI();
			log.error(String.format("%s denied to access (%s) %s.", username, method, url), accessDeniedException);
			response.sendRedirect("/login");
		});
	}

	private AuthenticationFailureHandler authenticationFailureHandler() {
		RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		return (request, response, exception) -> {
			log.error("authentication failure.", exception);
			redirectStrategy.sendRedirect(request, response, "/login");
		};
	}
}

package com.gmoon.resourceclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.gmoon.resourceclient.security.OAuthenticationFilter;
import com.gmoon.resourceclient.security.OAuthenticationProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			 .exceptionHandling(this::exceptionHandling)
			 .headers(this::headers)
			 .authorizeRequests(this::authorizeRequests)
			 .authenticationProvider(authenticationProvider())
			 .addFilter(new OAuthenticationFilter(authenticationManager(), authenticationFailureHandler()))
			 .formLogin();
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

	private void headers(HeadersConfigurer<HttpSecurity> config) {
		config.frameOptions().sameOrigin();
	}

	private void authorizeRequests(
		 ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
		registry.anyRequest().authenticated();
	}

	private AuthenticationFailureHandler authenticationFailureHandler() {
		RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		return (request, response, exception) -> {
			log.error("authentication failure.", exception);
			redirectStrategy.sendRedirect(request, response, "/login");
		};
	}
}

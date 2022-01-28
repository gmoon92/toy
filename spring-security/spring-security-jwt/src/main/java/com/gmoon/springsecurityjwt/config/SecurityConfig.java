package com.gmoon.springsecurityjwt.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gmoon.springsecurityjwt.jwt.JwtAuthenticationFilter;
import com.gmoon.springsecurityjwt.jwt.JwtExceptionHandler;
import com.gmoon.springsecurityjwt.jwt.JwtUtil;
import com.gmoon.springsecurityjwt.jwt.JwtVerifyFilter;
import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Configurable
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final MappingJackson2HttpMessageConverter converter;
	private final JwtUtil jwtUtil;

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.headers(headers -> headers.frameOptions().sameOrigin())
			.csrf().disable()
			.cors().and()
			.httpBasic().disable()
			.formLogin().disable()
			.exceptionHandling(handling ->
				handling.accessDeniedHandler(jwtExceptionHandler())
					.authenticationEntryPoint(jwtExceptionHandler()))
			.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeRequests(request ->
				request
					.antMatchers("/", "**/login**").permitAll()
					.antMatchers(HttpMethod.DELETE, "**").hasRole(Role.ADMIN.name())
					.anyRequest().authenticated())
			.addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtUtil))
			.addFilter(new JwtVerifyFilter(jwtExceptionHandler(), jwtUtil));
	}

	@Bean
	public JwtExceptionHandler jwtExceptionHandler() {
		return new JwtExceptionHandler(converter);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return SecurityUtils.PASSWORD_ENCODER;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", getCorsConfiguration());
		return source;
	}

	private CorsConfiguration getCorsConfiguration() {
		CorsConfiguration config = new CorsConfiguration();
		config.applyPermitDefaultValues();
		return config;
	}
}

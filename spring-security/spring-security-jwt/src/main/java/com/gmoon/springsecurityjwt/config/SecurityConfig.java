package com.gmoon.springsecurityjwt.config;

import com.gmoon.springsecurityjwt.jwt.JwtAuthenticationFilter;
import com.gmoon.springsecurityjwt.jwt.JwtExceptionHandler;
import com.gmoon.springsecurityjwt.jwt.JwtUtil;
import com.gmoon.springsecurityjwt.jwt.JwtVerifyFilter;
import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final MappingJackson2HttpMessageConverter converter;
	private final JwtUtil jwtUtil;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class)
			 .build();

		return http
			 .authenticationManager(authenticationManager)
			 .headers(headers -> headers
				  .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
			 )
			 .csrf(AbstractHttpConfigurer::disable)
			 .cors(Customizer.withDefaults())
			 .httpBasic(AbstractHttpConfigurer::disable)
			 .formLogin(AbstractHttpConfigurer::disable)
			 .exceptionHandling(handling ->
				  handling.accessDeniedHandler(jwtExceptionHandler())
					   .authenticationEntryPoint(jwtExceptionHandler()))
			 .sessionManagement(
				  sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			 .authorizeHttpRequests(authorization ->
				  authorization
					   .requestMatchers("/", "/login").permitAll()
					   .requestMatchers(HttpMethod.DELETE, "**").hasRole(Role.ADMIN.name())
					   .anyRequest().authenticated())
			 .addFilter(new JwtAuthenticationFilter(authenticationManager, jwtUtil))
			 .addFilter(new JwtVerifyFilter(jwtExceptionHandler(), jwtUtil))
			 .build();
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

package com.gmoon.resourceserver.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.gmoon.resourceserver.jwt.JwtAuthenticationFilter;
import com.gmoon.resourceserver.jwt.JwtExceptionHandler;
import com.gmoon.resourceserver.jwt.JwtUtils;
import com.gmoon.resourceserver.jwt.JwtVerifyFilter;
import com.gmoon.resourceserver.user.UserService;
import com.gmoon.resourceserver.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CorsConfigurationSource corsConfigurationSource;
	private final MappingJackson2HttpMessageConverter converter;
	private final JwtUtils jwtUtils;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public AuthenticationManager authenticationManager(UserService userService) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(userService);
		return new ProviderManager(provider);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws
		 Exception {
		return http
			 .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .cors(this::customCorsConfiguration)
			 .csrf(AbstractHttpConfigurer::disable)
			 .httpBasic(AbstractHttpConfigurer::disable)
			 .formLogin(AbstractHttpConfigurer::disable)
			 .authorizeHttpRequests(request -> request
				  .requestMatchers("**/login**").permitAll()
				  .anyRequest().authenticated())
			 .sessionManagement(this::httpStateless)
			 .addFilter(new JwtAuthenticationFilter(authenticationManager, jwtUtils))
			 .addFilter(new JwtVerifyFilter(jwtExceptionHandler(), jwtUtils))
			 .build();
	}

	private void httpStateless(SessionManagementConfigurer<HttpSecurity> configurer) {
		configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	private void customCorsConfiguration(CorsConfigurer<HttpSecurity> config) {
		config.configurationSource(corsConfigurationSource);
	}

	private JwtExceptionHandler jwtExceptionHandler() {
		return new JwtExceptionHandler(converter);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return SecurityUtils.PASSWORD_ENCODER;
	}
}

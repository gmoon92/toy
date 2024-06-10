package com.gmoon.jacoco.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.gmoon.jacoco.users.application.UserService;
import com.gmoon.jacoco.users.domain.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserService userService;

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain staticResourceChain(HttpSecurity http) throws Exception {
		return http
			 .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .securityMatcher(PathRequest.toStaticResources().atCommonLocations())
			 .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
			 .csrf(AbstractHttpConfigurer::disable)
			 .requestCache(AbstractHttpConfigurer::disable)
			 .securityContext(AbstractHttpConfigurer::disable)
			 .sessionManagement(AbstractHttpConfigurer::disable)
			 .build();
	}

	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			 .csrf(AbstractHttpConfigurer::disable)
			 .authenticationManager(authenticationManager())
			 .authorizeHttpRequests(authorize -> authorize
				  .requestMatchers(AntPathRequestMatcher.antMatcher("/users/**")).hasAnyRole(Role.Constants.ADMIN)
				  .anyRequest().authenticated()
			 )
			 .formLogin(Customizer.withDefaults())
			 .build();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(userService);
		return new ProviderManager(provider);
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder)PasswordEncoderFactories.createDelegatingPasswordEncoder();
		passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
		return passwordEncoder;
	}
}

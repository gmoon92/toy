package com.gmoon.springpoi.base.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.gmoon.springpoi.users.application.UserService;
import com.gmoon.springpoi.users.domain.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CorsConfigurationSource corsConfigurationSource;
	private final MappingJackson2HttpMessageConverter converter;

	@Bean
	public AuthenticationManager authenticationManager(UserService userService) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
		provider.setPasswordEncoder(passwordEncoder());
		return new ProviderManager(provider);
	}

	@Bean
	public SecurityFilterChain filterChain(
		 HttpSecurity http,
		 AuthenticationManager authenticationManager
	) throws Exception {
		return http
			 .authenticationManager(authenticationManager)
			 .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .cors(config -> config.configurationSource(corsConfigurationSource))

			 // CSRF 보호 설정.
			 // 운영 환경(웹/폼 인증 사용 시)에는 반드시 활성화(ON)할 것!
			 // REST API만 제공하거나 테스트 환경일 때만 OFF로 설정
			 .csrf(AbstractHttpConfigurer::disable)

			 // HTTP Basic 인증 비활성화.
			 // 폼 로그인(html form) 등 커스텀 인증 로직을 사용하는 경우 보통 비활성화.
			 // 단순 API(툴/내부서비스에서 Basic Auth 필요할 때만 활성)
			 .httpBasic(AbstractHttpConfigurer::disable)
			 .formLogin(Customizer.withDefaults())
			 .authorizeHttpRequests(request -> request
				  .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
				  .requestMatchers("/login**").permitAll()
				  .requestMatchers("/excel/**").hasRole(Role.ADMIN.name())
				  .anyRequest().authenticated()
			 )
			 .sessionManagement(this::statefulHttpSession)
			 .build();
	}

	private void statefulHttpSession(SessionManagementConfigurer<HttpSecurity> configurer) {
		configurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}


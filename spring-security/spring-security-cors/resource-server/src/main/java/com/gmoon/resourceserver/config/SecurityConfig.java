package com.gmoon.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import com.gmoon.resourceserver.jwt.JwtAuthenticationFilter;
import com.gmoon.resourceserver.jwt.JwtExceptionHandler;
import com.gmoon.resourceserver.jwt.JwtUtils;
import com.gmoon.resourceserver.jwt.JwtVerifyFilter;
import com.gmoon.resourceserver.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final CorsConfigurationSource corsConfigurationSource;
	private final MappingJackson2HttpMessageConverter converter;
	private final JwtUtils jwtUtils;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.headers(this::headers)
			.cors(this::customCorsConfiguration)
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.authorizeRequests(this::authorizeRequests)
			.sessionManagement(this::httpStateless)
			.addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtUtils))
			.addFilter(new JwtVerifyFilter(jwtExceptionHandler(), jwtUtils));
	}

	private void headers(HeadersConfigurer<HttpSecurity> config) {
		config.frameOptions().sameOrigin();
	}

	private void authorizeRequests(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
		registry
			.antMatchers("**/login**").permitAll()
			.anyRequest().authenticated();
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

package com.gmoon.springsecuritycsrfaspect.config;

import com.gmoon.springsecuritycsrfaspect.login.CustomAuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			 .headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
			 .csrf(AbstractHttpConfigurer::disable)
			 .cors(AbstractHttpConfigurer::disable)
			 .authorizeHttpRequests(request ->
				  // request.mvcMatchers("**/user/**").hasRole("ADMIN")
				  request
					   .requestMatchers(AntPathRequestMatcher.antMatcher("**/user/**")).hasRole("ADMIN")
					   .anyRequest()
					   .permitAll()

			 )
			 .exceptionHandling(configurer ->
				  configurer.accessDeniedHandler(accessDeniedHandler())
			 )
			 .formLogin(formLogin ->
				  formLogin.loginPage("/login")
					   .defaultSuccessUrl("/user/info")
					   .successHandler(customSuccessHandler())
			 )
			 .build();
	}

	@Bean
	public AuthenticationSuccessHandler customSuccessHandler() {
		return new CustomAuthenticationSuccessHandler();
	}

	private AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication authentication = context.getAuthentication();
			UserDetails user = (UserDetails)authentication.getPrincipal();

			String username = user.getUsername();
			String method = request.getMethod();
			String url = request.getRequestURI();
			String errorMessage = String.format("%s denied to access (%s) %s.", username, method, url);
			log.error(errorMessage, accessDeniedException);

			response.sendRedirect("/error/403");
		};
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}

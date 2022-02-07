package com.gmoon.resourceserver.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.gmoon.resourceserver.cors.CorsOriginService;
import com.gmoon.resourceserver.filter.CustomCorsFilter;
import com.gmoon.resourceserver.properties.CorsProperties;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static final String PATTERN_OF_CORS_CHECKED_URL = "/**";
	private static final long SECONDS_OF_PRE_FLIGHT_MAX_AGE = 3_600L;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.cors().and()
			.csrf().disable()
			.authorizeRequests(requests
				-> requests.anyRequest().permitAll())
			.sessionManagement(sessionManagement
				-> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
	}

	@Bean
	public CorsFilter corsFilter(CorsOriginService service, CorsProperties properties) {
		return new CustomCorsFilter(corsConfigurationSource(), service, properties);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		CorsConfiguration config = getCorsConfiguration();
		source.registerCorsConfiguration(PATTERN_OF_CORS_CHECKED_URL, config);
		return source;
	}

	private CorsConfiguration getCorsConfiguration() {
		CorsConfiguration config = new CorsConfiguration()
			.applyPermitDefaultValues();
		config.setMaxAge(SECONDS_OF_PRE_FLIGHT_MAX_AGE);
		config.setAllowedMethods(getCorsAllowedMethods());
		config.setAllowedOriginPatterns(Collections.singletonList(CorsConfiguration.ALL));
		// Access-Control-Allow-Credentials
		config.setAllowCredentials(true);
		return config;
	}

	private List<String> getCorsAllowedMethods() {
		return CorsProperties.ALL_OF_HTTP_METHODS;
	}
}

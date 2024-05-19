package com.gmoon.resourceserver.config;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.gmoon.resourceserver.cors.CorsOriginService;
import com.gmoon.resourceserver.filter.CustomCorsFilter;
import com.gmoon.resourceserver.properties.CorsProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties({CorsProperties.class})
@PropertySource(value = "classpath:cors.yml", factory = YamlPropertySourceFactory.class)
@RequiredArgsConstructor
public class CorsConfig {
	private static final String PATTERN_OF_CORS_CHECKED_URL = "/**";
	private static final long SECONDS_OF_PRE_FLIGHT_MAX_AGE = 3_600L;

	private final CorsOriginService service;

	@Bean
	public CorsFilter corsFilter(CorsOriginService service, CorsProperties properties) {
		CorsConfigurationSource configSource = corsConfigurationSource();

		return new CustomCorsFilter(configSource, service, properties);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		CorsConfiguration config = new CorsConfiguration(corsConfiguration());
		source.registerCorsConfiguration(PATTERN_OF_CORS_CHECKED_URL, config);
		return source;
	}

	@Bean
	public CorsConfiguration corsConfiguration() {
		CorsConfiguration config = new CorsConfiguration()
			 .applyPermitDefaultValues();
		config.setMaxAge(SECONDS_OF_PRE_FLIGHT_MAX_AGE);
		config.setAllowedMethods(getCorsAllowedMethods());
		config.setAllowedOriginPatterns(Collections.singletonList(CorsConfiguration.ALL));
		// Access-Control-Allow-Credentials
		config.setAllowCredentials(true);

		// No caching
		config.setMaxAge(Duration.ofSeconds(1));
		return config;
	}

	private List<String> getCorsAllowedMethods() {
		return service.getAllowedHttpMethods();
	}
}

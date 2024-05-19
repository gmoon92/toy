package com.gmoon.resourceserver.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.gmoon.resourceserver.cors.CorsOriginService;
import com.gmoon.resourceserver.properties.CorsProperties;
import com.gmoon.resourceserver.util.RequestUtils;

import lombok.extern.slf4j.Slf4j;

/***
 * @see org.springframework.web.cors.UrlBasedCorsConfigurationSource
 * @see CorsFilter#doFilterInternal
 * @see org.springframework.web.cors.CorsProcessor#processRequest(CorsConfiguration, HttpServletRequest, HttpServletResponse)
 * @see org.springframework.web.cors.DefaultCorsProcessor#processRequest(CorsConfiguration, HttpServletRequest, HttpServletResponse)
 * @see CorsConfiguration#checkOrigin
 */
@Slf4j
public class CustomCorsFilter extends CorsFilter {
	private final CorsConfigurationSource configSource;
	private final CorsOriginService service;
	private final CorsProperties properties;

	public CustomCorsFilter(CorsConfigurationSource configSource, CorsOriginService service,
		 CorsProperties properties) {
		super(configSource);
		this.configSource = configSource;
		this.service = service;
		this.properties = properties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		 FilterChain filterChain) throws ServletException, IOException {
		loggingForClientRequest(request);

		CorsConfiguration config = obtainCorsConfiguration(request);

		log.info("=============CORS Config=============");
		log.info("Allowed Headers: {}", config.getAllowedHeaders());
		log.info("Allowed Credentials: {}", config.getAllowCredentials());
		log.info("Allowed Method: {}", config.getAllowedMethods());
		log.info("Allowed Origin: {}", config.getAllowedOrigins());
		log.info("Allowed Origin Patterns: {}", config.getAllowedOriginPatterns());
		log.info("Max Age: {}", config.getMaxAge());
		log.info("=============CORS Config=============");
		super.doFilterInternal(request, response, filterChain);
	}

	private void loggingForClientRequest(HttpServletRequest request) {
		String scheme = request.getScheme();
		String remoteAddr = request.getRemoteAddr();
		int remotePort = request.getRemotePort();

		String method = request.getMethod();
		String requestURI = request.getRequestURI();
		log.info("origin: {}://{}:{} uri: [{}] {}", scheme, remoteAddr, remotePort, method, requestURI);
	}

	private CorsConfiguration obtainCorsConfiguration(HttpServletRequest request) {
		CorsConfiguration config = configSource.getCorsConfiguration(request);

		List<String> allowedOriginPatterns = getAllowedOriginPatterns(request);
		config.setAllowedOriginPatterns(allowedOriginPatterns);
		config.setAllowedMethods(getAllowedHttpMethods());
		return config;
	}

	private List<String> getAllowedOriginPatterns(HttpServletRequest request) {
		if (skipCheckedOriginUri(request)) {
			String host = RequestUtils.getOriginHost(request);
			return service.getAllowedOriginPatterns(Collections.singletonList(host));
		}

		return service.getAllowedOriginPatterns();
	}

	private boolean skipCheckedOriginUri(HttpServletRequest request) {
		boolean isCheckCorsOrigin = properties.isEnabled();
		String requestOrigin = RequestUtils.getOrigin(request);
		return isCheckCorsOrigin && StringUtils.isNotBlank(requestOrigin);
	}

	private List<String> getAllowedHttpMethods() {
		return service.getAllowedHttpMethods();
	}
}

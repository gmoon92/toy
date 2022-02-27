package com.gmoon.resourceserver.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.resourceserver.properties.CorsProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorsUtils {
	private static final String PATTERN_OF_ORIGIN = "**://%s:**";
	private static final String PATTERN_OF_IGNORE_PORT_ORIGIN = "**://%s";

	private final CorsProperties properties;

	public List<String> getAllowedOriginPatterns(List<String> hosts) {
		List<String> result = new ArrayList<>(hosts.size() * 2);
		for (String host : hosts) {
			result.add(String.format(PATTERN_OF_ORIGIN, host));
			result.add(String.format(PATTERN_OF_IGNORE_PORT_ORIGIN, host));
		}
		return result;
	}

	public boolean isEnabledCheckCorsRequest(HttpServletRequest request) {
		boolean isCheckCorsOrigin = properties.isEnabled();
		String requestOrigin = RequestUtils.getOrigin(request);
		return isCheckCorsOrigin && StringUtils.isNotBlank(requestOrigin);
	}
}

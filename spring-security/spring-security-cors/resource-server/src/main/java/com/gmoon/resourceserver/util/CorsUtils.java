package com.gmoon.resourceserver.util;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CorsUtils {
	private final String PATTERN_OF_ORIGIN = "**://%s:**";
	private final String PATTERN_OF_IGNORE_PORT_ORIGIN = "**://%s";

	public List<String> getAllowedOriginPatterns(List<String> hosts) {
		List<String> result = new ArrayList<>(hosts.size() * 2);
		for (String host : hosts) {
			result.add(String.format(PATTERN_OF_ORIGIN, host));
			result.add(String.format(PATTERN_OF_IGNORE_PORT_ORIGIN, host));
		}
		return result;
	}
}

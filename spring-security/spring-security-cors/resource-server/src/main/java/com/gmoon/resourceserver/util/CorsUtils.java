package com.gmoon.resourceserver.util;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CorsUtils {
	private static final String PATTERN_OF_ORIGIN = "**://%s:**";
	private static final String PATTERN_OF_IGNORE_PORT_ORIGIN = "**://%s";

	public static List<String> getAllowedOriginPatterns(List<String> hosts) {
		List<String> result = new ArrayList<>(hosts.size() * 2);
		for (String host : hosts) {
			result.add(String.format(PATTERN_OF_ORIGIN, host));
			result.add(String.format(PATTERN_OF_IGNORE_PORT_ORIGIN, host));
		}
		return result;
	}
}

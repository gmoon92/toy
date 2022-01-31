package com.gmoon.resourceserver.cors;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CorsOriginService {
	private static final String PATTERN_OF_ORIGIN = "**://%s:**";
	private static final String PATTERN_OF_IGNORE_PORT_ORIGIN = "**://%s";

	private final CorsOriginRepository repository;

	@Transactional(readOnly = true)
	@Cacheable(value = "corsConfig", key = "#root.methodName")
	public List<String> getAllowedOriginPatterns() {
		List<String> hosts = repository.getAllHost();

		List<String> result = new ArrayList<>(hosts.size() * 2);
		for (String host : hosts) {
			result.add(String.format(PATTERN_OF_ORIGIN, host));
			result.add(String.format(PATTERN_OF_IGNORE_PORT_ORIGIN, host));
		}
		return result;
	}
}

package com.gmoon.resourceserver.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;

import lombok.Getter;

@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
	public static final List<String> ALL_OF_HTTP_METHODS;

	static {
		List<String> httpMethodNames = new ArrayList<>(EnumSet.allOf(HttpMethod.class))
			.stream()
			.map(HttpMethod::name)
			.collect(Collectors.toList());
		ALL_OF_HTTP_METHODS = Collections.unmodifiableList(httpMethodNames);
	}

	private final boolean enabled;
	private final List<String> accessControlAllowMethods;

	protected CorsProperties(Boolean enabled, List<String> accessControlAllowMethods) {
		this.enabled = Boolean.TRUE.equals(enabled);
		this.accessControlAllowMethods = getHttpMethodAllIfEmpty(accessControlAllowMethods);
	}

	private List<String> getHttpMethodAllIfEmpty(List<String> accessControlAllowMethods) {
		boolean emptyHttpMethods = CollectionUtils.isEmpty(accessControlAllowMethods);
		boolean isAllPattern = CollectionUtils.containsAny(accessControlAllowMethods, CorsConfiguration.ALL);
		if (emptyHttpMethods || isAllPattern) {
			return ALL_OF_HTTP_METHODS;
		}

		List<String> upperCases = accessControlAllowMethods.stream()
			.map(StringUtils::trim)
			.filter(StringUtils::isNotBlank)
			.map(String::toUpperCase)
			.collect(Collectors.toList());

		return new ArrayList<>(CollectionUtils.retainAll(ALL_OF_HTTP_METHODS, upperCases));
	}
}

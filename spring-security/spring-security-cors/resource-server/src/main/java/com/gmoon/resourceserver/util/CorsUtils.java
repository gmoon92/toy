package com.gmoon.resourceserver.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.gmoon.resourceserver.properties.CorsProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorsUtils {
	private final CorsProperties properties;

	private boolean skipCheckedOriginUri(HttpServletRequest request) {
		return !properties.isEnabled()
			&& StringUtils.isBlank(RequestUtils.getOrigin(request));
	}
}

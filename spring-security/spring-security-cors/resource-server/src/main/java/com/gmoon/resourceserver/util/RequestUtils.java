package com.gmoon.resourceserver.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import constants.HttpHeaders;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {
	private static final String DELIMITER_OF_IP = ",";

	public static String getClientIpAddress(HttpServletRequest request) {
		String remoteIpAddress = getRemoteIpAddress(request);
		String[] ipAddresses = StringUtils.split(remoteIpAddress, DELIMITER_OF_IP);
		return ipAddresses[0];
	}

	private static String getRemoteIpAddress(HttpServletRequest request) {
		String originIpAddress = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
		String remoteIpAddress = request.getRemoteAddr();
		log.info("originIpAddress: {}, remoteIpAddress: {}", originIpAddress, remoteIpAddress);
		return StringUtils.defaultIfBlank(originIpAddress, remoteIpAddress);
	}
}

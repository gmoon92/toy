package com.gmoon.resourceserver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.resourceserver.constants.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {
	private static final String DELIMITER_OF_IP = ",";

	private static final int PATTERN_GROUP_OF_SCHEMA = 1;
	private static final int PATTERN_GROUP_OF_HOST = 2;
	private static final int PATTERN_GROUP_OF_PORT = 3;
	private static final String PATTERN_OF_SCHEMA = "(^\\w+(?=:\\/\\/))";
	private static final String PATTERN_OF_HOST = "((?<=:\\/\\/).*?(?=:|$))";
	private static final String PATTERN_OF_PORT = "((?<=:)\\d+|$)";
	private static final Pattern PATTERN_OF_ORIGIN = Pattern.compile(PATTERN_OF_SCHEMA
		 + ".*?" + PATTERN_OF_HOST
		 + ".*?" + PATTERN_OF_PORT);

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

	public static String getOrigin(HttpServletRequest request) {
		String origin = request.getHeader(HttpHeaders.ORIGIN);
		log.info("origin: {}", origin);
		return StringUtils.defaultString(origin);
	}

	public static String getOriginSchema(HttpServletRequest request) {
		return getOriginPatternGroup(getOrigin(request), PATTERN_GROUP_OF_SCHEMA);
	}

	public static String getOriginHost(HttpServletRequest request) {
		return getOriginPatternGroup(getOrigin(request), PATTERN_GROUP_OF_HOST);
	}

	public static Integer getOriginPort(HttpServletRequest request) {
		String port = getOriginPatternGroup(getOrigin(request), PATTERN_GROUP_OF_PORT);
		if (StringUtils.isBlank(port)) {
			return null;
		}

		return Integer.parseInt(port);
	}

	private static String getOriginPatternGroup(String origin, int group) {
		Matcher m = PATTERN_OF_ORIGIN.matcher(origin);
		if (m.find()) {
			return m.group(group);
		}

		return "";
	}
}

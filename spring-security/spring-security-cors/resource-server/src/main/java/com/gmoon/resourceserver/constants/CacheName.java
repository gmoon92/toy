package com.gmoon.resourceserver.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
@Getter
public enum CacheName {
	ALLOWED_HTTP_METHODS(Constants.ALLOWED_HTTP_METHODS),
	ALLOWED_ORIGIN_PATTERN(Constants.ALLOWED_ORIGIN_PATTERN);

	private final String value;

	@UtilityClass
	public class Constants {
		public static final String ALLOWED_HTTP_METHODS = "ALLOWED_HTTP_METHODS";
		public static final String ALLOWED_ORIGIN_PATTERN = "ALLOWED_ORIGIN_PATTERN";
	}
}

package com.gmoon.resourceserver.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@Getter
@RequiredArgsConstructor
public enum CacheName {
	CORS_CONFIG(Constants.CORS_CONFIG);

	private final String value;

	@UtilityClass
	public class Constants {
		public final String CORS_CONFIG = "CORS_CONFIG";
	}
}

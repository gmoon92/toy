package com.gmoon.springdataredis.cache;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
public enum CachePolicy {
	CONFIG(Name.SYSTEM_CONFIG),
	USER(Name.USER);

	public final String name;
	public final Duration ttl;

	private static final long TTL_OF_MINUTES = 5L;
	public static final Duration DEFAULT_TTL = Duration.ofMinutes(TTL_OF_MINUTES);

	CachePolicy(String name) {
		this(name, Duration.ofMinutes(TTL_OF_MINUTES));
	}

	@UtilityClass
	public class Name {
		public final String SYSTEM_CONFIG = "CONFIG";
		public final String USER = "USER";
	}
}

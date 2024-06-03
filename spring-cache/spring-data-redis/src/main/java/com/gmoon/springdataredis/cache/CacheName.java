package com.gmoon.springdataredis.cache;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
@Getter
public enum CacheName {
	CONFIG(Constants.CONFIG),
	COMMON(Constants.COMMON),
	USER(Constants.USER);

	private static final Set<String> ALL = Collections.unmodifiableSet(Stream.of(values())
		 .map(CacheName::getValue)
		 .collect(Collectors.toSet()));

	private final String value;

	public static Set<String> getAll() {
		return ALL;
	}

	@UtilityClass
	public class Constants {
		private final String VERSION = "V2";
		public final String CONFIG = VERSION + "CONFIG";
		public final String COMMON = VERSION + "COMMON";
		public final String USER = VERSION + "USER";
	}
}

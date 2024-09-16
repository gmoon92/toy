package com.gmoon.javacore.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@UtilityClass
public final class TimeUtils {

	public static Instant from(long epochMilli) {
		return Instant.ofEpochMilli(epochMilli);
	}

	public static Instant of(String pattern, String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		TemporalAccessor parse = formatter.parse(dateString);
		return LocalDateTime.from(parse)
			 .toInstant(ZoneOffset.UTC);
	}
}

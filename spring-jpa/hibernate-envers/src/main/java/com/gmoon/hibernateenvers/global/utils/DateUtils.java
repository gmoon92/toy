package com.gmoon.hibernateenvers.global.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public final class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private final static int MIN_YEAR = 1;
	private final static int MAX_YEAR = 9999;

	public static Date min() {
		LocalDate localDate = LocalDate.of(MIN_YEAR, 1, 1);
		Instant instant = localDate.atStartOfDay(ZoneId.systemDefault())
			 .toInstant();
		return Date.from(instant);
	}

	public static Date max() {
		LocalDate localDate = LocalDate.of(MAX_YEAR, 12, 31);
		Instant instant = localDate.atStartOfDay(ZoneId.systemDefault())
			 .toInstant();
		return Date.from(instant);
	}

	public static Date of(LocalDate localDate) {
		return of(localDate.atStartOfDay(ZoneId.systemDefault()));
	}

	public static Date of(ZonedDateTime zonedDateTime) {
		Instant instant = zonedDateTime.toInstant();
		return Date.from(instant);
	}
}

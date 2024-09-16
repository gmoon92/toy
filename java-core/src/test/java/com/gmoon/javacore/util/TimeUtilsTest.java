package com.gmoon.javacore.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TimeUtilsTest {

	@Test
	void from() {
		assertThatCode(() -> TimeUtils.from(System.currentTimeMillis()))
			 .doesNotThrowAnyException();
	}

	@Test
	void dataFormat() {
		Instant instant = TimeUtils.of("yyyy-MM-dd HH:mm:ss", "2024-09-16 15:00:00");

		assertThat(DateTimeFormatter.ISO_INSTANT.format(instant))
			 .isEqualTo("2024-09-16T15:00:00Z");
	}
}

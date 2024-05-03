package com.gmoon.querydslprojections.movies.movie.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class MovieReleaseTimeTest {

	@Test
	void create() {
		LocalDateTime dateTime = LocalDateTime.of(
			2024,
			4,
			30,
			15,
			0
		);

		MovieReleaseTime releaseTime = new MovieReleaseTime(dateTime);

		assertThat(releaseTime)
			.hasNoNullFieldsOrProperties()
			.hasFieldOrPropertyWithValue("value", dateTime)
			.hasFieldOrPropertyWithValue("year", 2024)
			.hasFieldOrPropertyWithValue("month", 4)
			.hasFieldOrPropertyWithValue("dayOfMonth", 30)
			.hasFieldOrPropertyWithValue("hour", 15);
	}
}

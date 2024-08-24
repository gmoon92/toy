package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MovieReleaseTime implements Serializable {

	private Instant value;

	private int year;

	private int month;

	private int dayOfMonth;

	private int hour;

	public MovieReleaseTime(Instant value) {
		this.value = value;

		LocalDateTime ldt = LocalDateTime.ofInstant(value, ZoneOffset.UTC);
		this.year = ldt.getYear();
		this.month = ldt.getMonthValue();
		this.dayOfMonth = ldt.getDayOfMonth();
		this.hour = ldt.getHour();
	}

	public long toSeconds() {
		return value.getEpochSecond();
	}
}

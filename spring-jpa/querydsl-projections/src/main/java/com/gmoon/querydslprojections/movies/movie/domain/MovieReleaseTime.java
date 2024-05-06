package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class MovieReleaseTime implements Serializable {

	private LocalDateTime value;

	private int year;

	private int month;

	private int dayOfMonth;

	private int hour;

	public MovieReleaseTime(LocalDateTime value) {
		this.value = value;
		this.year = value.getYear();
		this.month = value.getMonthValue();
		this.dayOfMonth = value.getDayOfMonth();
		this.hour = value.getHour();
	}

	public long toSeconds() {
		return value.toEpochSecond(ZoneOffset.UTC);
	}
}

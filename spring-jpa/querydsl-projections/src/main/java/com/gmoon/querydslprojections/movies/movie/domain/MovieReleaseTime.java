package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

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

	private LocalDateTime releaseTime;

	private int year;

	private int month;

	private int dayOfMonth;

	private int hour;

	public MovieReleaseTime(LocalDateTime releaseTime) {
		this.releaseTime = releaseTime;
		this.year = releaseTime.getYear();
		this.month = releaseTime.getMonthValue();
		this.dayOfMonth = releaseTime.getDayOfMonth();
		this.hour = releaseTime.getHour();
	}
}

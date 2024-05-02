package com.gmoon.querydslprojections.movies.movie.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.querydslprojections.movies.movie.domain.FilmRatings;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MovieResponse {

	private String id;
	private String name;
	private MovieGenre genre;
	private FilmRatings filmRatings;
	private String directorName;
	private List<String> castNames;
	private Long releaseDateTime;

	@Builder
	@QueryProjection
	public MovieResponse(String id, String name, MovieGenre genre, FilmRatings filmRatings, String directorName,
		List<String> castNames,
		LocalDateTime releaseDateTime) {
		this.id = id;
		this.name = name;
		this.genre = genre;
		this.filmRatings = filmRatings;
		this.directorName = directorName;
		this.castNames = castNames;
		this.releaseDateTime = releaseDateTime.toEpochSecond(ZoneOffset.UTC);
	}
}

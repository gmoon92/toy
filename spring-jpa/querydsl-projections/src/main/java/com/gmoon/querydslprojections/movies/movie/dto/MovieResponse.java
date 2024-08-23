package com.gmoon.querydslprojections.movies.movie.dto;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.querydslprojections.movies.movie.domain.FilmRating;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;
import com.gmoon.querydslprojections.movies.movie.domain.MovieReleaseTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MovieResponse {

	private String id;
	private String movieName;
	private MovieGenre genre;
	private FilmRating filmRating;
	private String directorName;
	private List<String> castNames;
	private Long releaseDateTime;

	@QueryProjection
	public MovieResponse(String id, String movieName, MovieGenre genre,
		 FilmRating filmRating, String movieDirectorName, MovieReleaseTime movieReleaseTime) {
		this.id = id;
		this.movieName = movieName;
		this.genre = genre;
		this.filmRating = filmRating;
		this.directorName = movieDirectorName;
		this.releaseDateTime = movieReleaseTime.toSeconds();
	}
}

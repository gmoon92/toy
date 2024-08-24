package com.gmoon.querydslprojections.global;

import java.time.Instant;
import java.util.UUID;

import com.gmoon.querydslprojections.movies.movie.domain.FilmRating;
import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;
import com.gmoon.querydslprojections.movies.movie.domain.MovieReleaseTime;
import com.gmoon.querydslprojections.movies.users.user.domain.Actor;
import com.gmoon.querydslprojections.movies.users.user.domain.Director;

import lombok.Builder;

public class Fixtures {

	public static String MOVIE_ID_001 = "movie001";
	public static String MOVIE_ID_002 = "movie002";

	@Builder(
		 builderMethodName = "newMovie",
		 setterPrefix = "with",
		 buildMethodName = "create"
	)
	public static Movie movie(
		 String name,
		 MovieGenre genre,
		 FilmRating filmRating
	) {
		return Movie.builder()
			 .id(uuid())
			 .name(name)
			 .genre(genre)
			 .filmRating(filmRating)
			 .releaseTime(new MovieReleaseTime(Instant.now()))
			 .build();
	}

	public static Director director(String name) {
		return new Director(uuid(), name);
	}

	public static Actor actor(String name) {
		return new Actor(uuid(), name);
	}

	private static String uuid() {
		return UUID.randomUUID().toString();
	}
}

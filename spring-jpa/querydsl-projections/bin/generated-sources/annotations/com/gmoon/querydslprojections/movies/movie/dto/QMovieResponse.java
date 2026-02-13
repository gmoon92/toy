package com.gmoon.querydslprojections.movies.movie.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.gmoon.querydslprojections.movies.movie.dto.QMovieResponse is a Querydsl Projection type for MovieResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMovieResponse extends ConstructorExpression<MovieResponse> {

    private static final long serialVersionUID = -651798229L;

    public QMovieResponse(com.querydsl.core.types.Expression<String> id, com.querydsl.core.types.Expression<String> movieName, com.querydsl.core.types.Expression<com.gmoon.querydslprojections.movies.movie.domain.MovieGenre> genre, com.querydsl.core.types.Expression<com.gmoon.querydslprojections.movies.movie.domain.FilmRating> filmRating, com.querydsl.core.types.Expression<String> movieDirectorName, com.querydsl.core.types.Expression<? extends com.gmoon.querydslprojections.movies.movie.domain.MovieReleaseTime> movieReleaseTime) {
        super(MovieResponse.class, new Class<?>[]{String.class, String.class, com.gmoon.querydslprojections.movies.movie.domain.MovieGenre.class, com.gmoon.querydslprojections.movies.movie.domain.FilmRating.class, String.class, com.gmoon.querydslprojections.movies.movie.domain.MovieReleaseTime.class}, id, movieName, genre, filmRating, movieDirectorName, movieReleaseTime);
    }

}


package com.gmoon.querydslprojections.movies.movie.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;

public interface JpaMovieRepository extends MovieRepository, JpaRepository<Movie, String> {
}

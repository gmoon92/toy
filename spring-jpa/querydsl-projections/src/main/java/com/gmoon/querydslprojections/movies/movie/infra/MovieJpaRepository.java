package com.gmoon.querydslprojections.movies.movie.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;

public interface MovieJpaRepository extends JpaRepository<Movie, String> {
}

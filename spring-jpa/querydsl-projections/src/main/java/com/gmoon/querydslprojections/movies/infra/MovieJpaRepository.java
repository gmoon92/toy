package com.gmoon.querydslprojections.movies.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.querydslprojections.movies.domain.Movie;

public interface MovieJpaRepository extends JpaRepository<Movie, String> {
}

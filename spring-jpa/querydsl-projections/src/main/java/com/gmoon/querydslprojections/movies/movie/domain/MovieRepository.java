package com.gmoon.querydslprojections.movies.movie.domain;

import java.util.List;
import java.util.Optional;

public interface MovieRepository {

	Movie save(Movie movie);

	Optional<Movie> findById(String id);

	List<Movie> findAll();
}

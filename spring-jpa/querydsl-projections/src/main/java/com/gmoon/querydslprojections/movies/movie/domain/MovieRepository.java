package com.gmoon.querydslprojections.movies.movie.domain;

import java.util.List;
import java.util.Optional;

public interface MovieRepository<T extends Movie, ID> {

	T save(T movie);

	Optional<T> findById(ID id);

	List<T> findAll();
}

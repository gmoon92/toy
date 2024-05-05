package com.gmoon.querydslprojections.movies.movie.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryMovieRepository implements MovieRepository {

	private static final Map<String, Movie> CACHE = Collections.synchronizedMap(new HashMap<>());

	@Override
	public Movie save(Movie movie) {
		String id = movie.getId();
		boolean newEntity = id == null;
		if (newEntity) {
			id = UUID.randomUUID().toString();
		}

		CACHE.put(id, movie);
		return movie;
	}

	@Override
	public Movie get(String id) {
		return CACHE.get(id);
	}

	@Override
	public Optional<Movie> findById(String id) {
		return Optional.ofNullable(CACHE.get(id));
	}

	@Override
	public List<Movie> findAll() {
		return new ArrayList<>(CACHE.values());
	}
}

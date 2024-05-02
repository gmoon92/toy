package com.gmoon.querydslprojections.movies.movie.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepository {

	private final JpaMovieRepository repository;

	@Override
	public Movie save(Movie movie) {
		return repository.save(movie);
	}

	@Override
	public Optional<Movie> findById(String id) {
		return repository.findById(id);
	}

	@Override
	public List<Movie> findAll() {
		return repository.findAll();
	}
}

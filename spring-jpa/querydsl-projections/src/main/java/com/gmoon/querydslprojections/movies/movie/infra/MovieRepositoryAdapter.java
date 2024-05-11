package com.gmoon.querydslprojections.movies.movie.infra;

import static com.gmoon.querydslprojections.movies.movie.domain.QMovie.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaMovieRepository repository;

	@Override
	public Movie save(Movie movie) {
		return repository.save(movie);
	}

	@Override
	public Movie get(String id) {
		return queryFactory
			.selectFrom(movie)
			.where(movie.id.eq(id))
			.fetchFirst();
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

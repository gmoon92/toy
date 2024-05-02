package com.gmoon.querydslprojections.movies.movie.application;

import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieCast;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;
import com.gmoon.querydslprojections.movies.movie.dto.MovieResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovieService {

	private final MovieRepository movieRepository;

	public MovieResponse getMovie(String id) {
		Movie movie = movieRepository.findById(id)
			.orElseThrow(EntityNotFoundException::new);

		return MovieResponse.builder()
			.id(movie.getId())
			.name(movie.getName())
			.genre(movie.getGenre())
			.filmRatings(movie.getFilmRatings())
			.directorName(movie.getDirector().getName())
			.releaseDateTime(movie.getReleaseTime().getValue())
			.castNames(movie.getCastMembers().stream()
				.map(MovieCast::getName)
				.collect(Collectors.toList()))
			.build();
	}
}

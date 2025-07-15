package com.gmoon.querydslprojections.movies.movie.application;

import org.springframework.stereotype.Service;

import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieCast;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;
import com.gmoon.querydslprojections.movies.movie.dto.MovieResponse;

import jakarta.persistence.EntityNotFoundException;
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
			 .movieName(movie.getName())
			 .genre(movie.getGenre())
			 .filmRating(movie.getFilmRating())
			 .directorName(movie.getDirectorName())
			 .releaseDateTime(movie.getReleaseTime().toSeconds())
			 .castNames(movie.getCastMembers().stream()
				  .map(MovieCast::getName)
				  .toList())
			 .build();
	}
}

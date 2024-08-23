package com.gmoon.querydslprojections.movies.movie.application;

import static com.gmoon.querydslprojections.global.Fixtures.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gmoon.querydslprojections.global.Fixtures;
import com.gmoon.querydslprojections.movies.movie.domain.FilmRating;
import com.gmoon.querydslprojections.movies.movie.domain.InMemoryMovieRepository;
import com.gmoon.querydslprojections.movies.movie.domain.Movie;
import com.gmoon.querydslprojections.movies.movie.domain.MovieGenre;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;
import com.gmoon.querydslprojections.movies.movie.dto.MovieResponse;

class MovieServiceTest {

	private MovieRepository movieRepository;
	private MovieService movieService;

	@BeforeEach
	void setUp() {
		movieRepository = new InMemoryMovieRepository();
		movieService = new MovieService(movieRepository);
	}

	@Test
	void getMovie() {
		Movie movie = movieRepository.save(newMovie());

		MovieResponse movieResponse = movieService.getMovie(movie.getId());

		assertThat(movieResponse)
			 .hasFieldOrPropertyWithValue("id", movie.getId())
			 .hasFieldOrPropertyWithValue("movieName", movie.getName())
			 .hasFieldOrPropertyWithValue("genre", movie.getGenre())
			 .hasFieldOrPropertyWithValue("filmRating", movie.getFilmRating())
			 .hasFieldOrPropertyWithValue("directorName", movie.getDirectorName())
			 .hasFieldOrProperty("castNames")
			 .hasFieldOrProperty("releaseDateTime")
			 .hasNoNullFieldsOrPropertiesExcept()
			 .satisfies(
				  response -> assertThat(response.getCastNames()).isNotEmpty()
					   .containsOnly("Daniel Radcliffe", "Emma Charlotte Duerre Watson", "Rupert Grint")
			 );
	}

	private Movie newMovie() {
		return Fixtures.newMovie()
			 .withName("Harry Potter")
			 .withGenre(MovieGenre.FICTION)
			 .withFilmRating(FilmRating.G)
			 .create()
			 .chooseDirector(director("Alfonso Cuaron"))
			 .castingActor(actor("Daniel Radcliffe"))
			 .castingActor(actor("Rupert Grint"))
			 .castingActor(actor("Emma Charlotte Duerre Watson"));
	}
}

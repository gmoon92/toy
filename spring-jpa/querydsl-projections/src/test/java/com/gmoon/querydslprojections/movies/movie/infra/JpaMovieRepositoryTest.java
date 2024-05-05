package com.gmoon.querydslprojections.movies.movie.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.gmoon.querydslprojections.global.Fixtures;
import com.gmoon.querydslprojections.global.JpaConfig;
import com.gmoon.querydslprojections.movies.movie.domain.MovieRepository;

@DataJpaTest(
	includeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE, classes = {
		JpaConfig.class,
		MovieRepositoryAdapter.class
	})
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaMovieRepositoryTest {

	@Autowired
	private MovieRepository repository;

	@Test
	void get() {
		assertThatCode(() -> repository.get(Fixtures.MOVIE_ID_001))
			.doesNotThrowAnyException();
	}

	@Test
	void findById() {
		assertThatCode(() -> repository.findById(Fixtures.MOVIE_ID_001))
			.doesNotThrowAnyException();
	}

	@Test
	void findAll() {
		assertThat(repository.findAll()).isNotEmpty();
	}
}

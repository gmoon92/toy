package com.gmoon.querydslprojections.movies.movie.infra;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaMovieRepositoryTest {

	@Autowired
	private JpaMovieRepository repository;

	@Test
	void findAll() {
		assertThat(repository.findAll()).isNotEmpty();
	}
}

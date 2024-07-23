package com.gmoon.dbcleaner.movies.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(CouponRepositoryAdapter.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCouponRepositoryTest {

	@Autowired
	private JpaCouponRepository repository;

	@Test
	void findAllByMovieId() {
		assertThat(repository.findAllByMovieId(1L))
			 .isNotEmpty();
	}
}

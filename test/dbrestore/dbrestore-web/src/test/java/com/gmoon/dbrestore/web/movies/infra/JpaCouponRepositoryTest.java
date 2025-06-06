package com.gmoon.dbrestore.web.movies.infra;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(CouponRepositoryAdapter.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCouponRepositoryTest {

	@Autowired
	private JpaCouponRepository repository;

	@Test
	void findAllByMovieId() {
		Assertions.assertThat(repository.findAllByMovieId(1L))
			 .isNotEmpty();
	}
}

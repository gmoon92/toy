package com.gmoon.dbrecovery.movies.infra;

import com.gmoon.dbrecovery.movies.domain.TicketOfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Import(TicketOfficeRepositoryAdapter.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaTicketOfficeRepositoryTest {

	@Autowired
	private TicketOfficeRepository repository;

	@Test
	void findAllByMovieId() {
		Pageable pageable = PageRequest.of(0, 10);

		assertThatCode(() -> repository.findAllByMovieId(1L, pageable))
			 .doesNotThrowAnyException();
	}

	@Test
	void findMovie() {
		long id = 1L;
		long movieId = 1L;

		assertThat(repository.findMovie(id, movieId))
			 .isNotEqualTo(Optional.empty());
	}
}
package com.gmoon.dbcleaner.movies.infra;

import com.gmoon.dbcleaner.movies.domain.TicketOfficeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
}

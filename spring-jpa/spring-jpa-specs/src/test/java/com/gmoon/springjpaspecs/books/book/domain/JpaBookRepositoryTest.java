package com.gmoon.springjpaspecs.books.book.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaBookRepositoryTest {

	@Autowired
	JpaBookRepository repository;

	@Test
	void create() {
		Book book = Book.create("gmoon");

		Book saved = repository.save(book);

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getName()).isEqualTo(new BookName("gmoon"));
	}

	@Test
	void name() {
		repository.findById(UUID.randomUUID());
		repository.findAllById(Arrays.asList(UUID.randomUUID()));
		repository.findAll();
	}
}

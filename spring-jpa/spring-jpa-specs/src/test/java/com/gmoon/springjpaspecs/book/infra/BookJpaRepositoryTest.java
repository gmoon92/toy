package com.gmoon.springjpaspecs.book.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmoon.springjpaspecs.book.domain.Book;
import com.gmoon.springjpaspecs.book.domain.BookName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class BookJpaRepositoryTest {

	@Autowired
	BookJpaRepository repository;

	@Test
	void create() {
		Book book = Book.create("gmoon");

		Book saved = repository.save(book);

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getName()).isEqualTo(new BookName("gmoon"));
	}
}

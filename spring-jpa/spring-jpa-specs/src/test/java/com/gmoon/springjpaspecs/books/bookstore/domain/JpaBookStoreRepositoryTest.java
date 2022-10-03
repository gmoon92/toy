package com.gmoon.springjpaspecs.books.bookstore.domain;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.global.vo.DataJpaTestSupport;

class JpaBookStoreRepositoryTest extends DataJpaTestSupport {

	@Autowired
	private JpaBookStoreRepository repository;

	@Test
	void create() {
		BookStore bookStore = new BookStore("gmoon-store1");
		bookStore.addBook(
			"book-no0",
			new BookQuantity(10),
			BookType.ETC
		);

		BookStore savedBookStore = repository.save(bookStore);

		assertThat(savedBookStore.getId()).isNotBlank();
		assertThat(savedBookStore.getName()).isNotBlank();
	}

	@Test
	void findById() {
		String id = "book-store-1";

		BookStore bookStore = repository.findById(id)
			.orElseThrow(EntityNotFoundException::new);

		assertThat(bookStore.getName()).isEqualTo("gmoons");
	}

	@AfterEach
	void tearDown() {
		flushAndClear();
	}
}

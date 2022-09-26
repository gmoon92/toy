package com.gmoon.springjpaspecs.books.bookstore.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookId;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaBookStoreRepositoryTest {

	@Autowired
	private JpaBookStoreRepository repository;

	@PersistenceContext
	private EntityManager em;

	@Test
	void create() {
		BookStore bookStore = new BookStore("gmoon-store1");
		bookStore.addBook(
			new BookId("book-no0"),
			new BookQuantity(10),
			BookType.ETC
		);

		BookStore savedBookStore = repository.save(bookStore);

		assertThat(savedBookStore.getId()).isNotBlank();
		assertThat(savedBookStore.getName()).isNotBlank();
	}

	@AfterEach
	void tearDown() {
		em.flush();
		em.clear();
	}
}

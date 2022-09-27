package com.gmoon.springjpaspecs.books.book.domain;

import static com.gmoon.springjpaspecs.books.Fixtures.book;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaBookRepositoryTest {

	@Autowired
	JpaBookRepository repository;

	@Test
	void create() {
		Book book = book("gmoon", BigDecimal.ONE);

		Book saved = repository.save(book);

		assertThat(saved).isNotNull();
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getName()).isEqualTo(new BookName("gmoon"));
	}

	/**
	 * error: reference to save is ambiguous
	 *         repository.save(new Product());
	 *                   ^
	 *   both method save(Product) in ProductRepository and method <S>save(S) in CrudRepository match
	 *   where S,T are type-variables:
	 *     S extends T declared in method <S>save(S)
	 *     T extends Object declared in interface CrudRepository
	 * */
	@Test
	void ambiguousReference() {
		repository.save(new Book());
		repository.findById("book-no0");
		repository.findAllById(Arrays.asList("book-no0"));
		repository.findAll();
	}

	@Test
	void findById() {
		Book book = repository.findById("book-no0").get();

		assertThat(book).isNotNull();
		assertAll(
			() -> assertThat(book.getId()).isNotBlank(),
			() -> assertThat(book.getName()).isEqualTo(new BookName("START! DDD")),
			() -> assertThat(book.getIsbn()).isNotNull(),
			() -> assertThat(book.getPrice().getValue().setScale(2)).isEqualTo(
				BigDecimal.valueOf(36_000).setScale(2)));
	}
}

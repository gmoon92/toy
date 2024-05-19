package com.gmoon.springjpaspecs.books.book.domain;

import static com.gmoon.springjpaspecs.books.Fixtures.*;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.global.domain.SupportDataJpaTest;

class JpaBookRepositoryTest extends SupportDataJpaTest {

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
		repository.findAllById(asList("book-no0"));
		repository.findAll();
	}

	@Test
	void findById() {
		Book book = repository.findById("book-no0").
			 orElseThrow(EntityNotFoundException::new);

		assertThat(book).isNotNull();
		assertAll(
			 () -> assertThat(book.getId()).isNotBlank(),
			 () -> assertThat(book.getName()).isEqualTo(new BookName("START! DDD")),
			 () -> assertThat(book.getIsbn()).isNotNull(),
			 () -> assertThat(book.getPrice()).isNotNull());
	}
}

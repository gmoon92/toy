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
		repository.findById(UUID.randomUUID());
		repository.findAllById(Arrays.asList(UUID.randomUUID()));
		repository.findAll();
	}
}
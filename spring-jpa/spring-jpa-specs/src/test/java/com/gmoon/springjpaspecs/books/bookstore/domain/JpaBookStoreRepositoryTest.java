package com.gmoon.springjpaspecs.books.bookstore.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.core.types.Order;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;
import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;
import com.gmoon.springjpaspecs.global.domain.SupportDataJpaTest;
import com.gmoon.springjpaspecs.global.specs.conditional.Specification;
import com.gmoon.springjpaspecs.global.specs.conditional.Specs;
import com.gmoon.springjpaspecs.global.specs.orderby.OrderSpecification;

import jakarta.persistence.EntityNotFoundException;

class JpaBookStoreRepositoryTest extends SupportDataJpaTest {

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

	@ParameterizedTest
	@EnumSource(SortTargetType.class)
	void findAll(SortTargetType sortTargetType) {
		OrderSpecification orderSpec = BookStoreOrderSpec.create(Order.ASC, sortTargetType);

		assertThatCode(() -> repository.findAll(orderSpec))
			 .doesNotThrowAnyException();
	}

	@SuppressWarnings("unchecked")
	@DisplayName("Java Spec 조합의 장점")
	@Test
	void specOfCompoundExpression() {
		List<BookStore> all = repository.findAll();

		Specification<BookStoreBook> isDisPlayed = Specs.<BookStoreBook>create()
			 .and(
				  BookStoreBook::isDisplay,
				  BookStoreBook::availableSale
			 );

		List<BookStoreBook> actual = all.stream()
			 .flatMap(bookStore -> bookStore.getStoredBooks().stream())
			 .filter(isDisPlayed::isSatisfiedBy)
			 .collect(Collectors.toList());

		assertThat(actual)
			 .map(BookStoreBook::getStatus)
			 .containsOnly(BookStatus.DISPLAY);
	}

	@AfterEach
	void tearDown() {
		flushAndClear();
	}
}

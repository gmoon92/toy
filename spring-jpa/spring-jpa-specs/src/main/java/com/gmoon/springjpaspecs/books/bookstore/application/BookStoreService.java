package com.gmoon.springjpaspecs.books.bookstore.application;

import static com.gmoon.springjpaspecs.books.book.domain.QBook.*;
import static com.gmoon.springjpaspecs.books.bookstore.domain.QBookStoreBook.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gmoon.springjpaspecs.books.bookstore.domain.BookStoreOrderSpec;
import com.gmoon.springjpaspecs.books.bookstore.domain.JpaBookStoreRepository;
import com.gmoon.springjpaspecs.books.bookstore.domain.spec.BookSpecs;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentRequest;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentResponse;
import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;
import com.gmoon.springjpaspecs.global.specs.orderby.OrderSpecification;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.Predicate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookStoreService {

	private final JpaBookStoreRepository bookStoreRepository;

	public List<BookStoreContentResponse> findAll(BookStoreContentRequest request) {
		BookStoreContentRequest.Search search = request.getSearch();
		Predicate predicates = getPredicates(search);

		Order order = request.getOrder();
		SortTargetType sortTargetType = request.getSortTargetType();
		OrderSpecification orderSpec = BookStoreOrderSpec.create(order, sortTargetType);
		return bookStoreRepository.findAll(predicates, orderSpec);
	}

	private Predicate getPredicates(BookStoreContentRequest.Search search) {
		Predicate isDisplayedBook = BookSpecs.display(bookStoreBook);
		BooleanBuilder predicate = new BooleanBuilder(isDisplayedBook)
			.and(book.name.value.contains(search.getName()));

		if (search.isDiscountBook()) {
			Predicate isDiscountBook = BookSpecs.discountBook(book);
			predicate.and(isDiscountBook);
		}
		return predicate;
	};
}

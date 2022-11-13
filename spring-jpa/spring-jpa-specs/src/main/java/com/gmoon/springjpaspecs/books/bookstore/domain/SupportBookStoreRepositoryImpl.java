package com.gmoon.springjpaspecs.books.bookstore.domain;

import static com.gmoon.springjpaspecs.books.book.domain.QBook.*;
import static com.gmoon.springjpaspecs.books.bookstore.domain.QBookStore.*;
import static com.gmoon.springjpaspecs.books.bookstore.domain.QBookStoreBook.*;

import java.util.List;

import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentResponse;
import com.gmoon.springjpaspecs.books.bookstore.dto.QBookStoreContentResponse;
import com.gmoon.springjpaspecs.global.specs.orderby.OrderSpecification;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SupportBookStoreRepositoryImpl implements SupportBookStoreRepository{

	private final JPAQueryFactory factory;

	@Override
	public List<BookStoreContentResponse> findAll(OrderSpecification orderSpec) {
		JPAQuery<BookStoreContentResponse> query = factory.select(new QBookStoreContentResponse(book, bookStoreBook))
			.from(bookStore)
			.join(bookStore.storedBooks, bookStoreBook)
			.join(book).on(book.id.eq(bookStoreBook.bookId))
			.where(bookStoreBook.status.eq(BookStatus.DISPLAY));
		orderSpec.orderBy(query);
		return query.fetch();
	}

	@Override
	public List<BookStore> findAll(Predicate predicate) {
		return factory.select(bookStore)
			.from(bookStore)
			.join(bookStore.storedBooks, bookStoreBook)
			.where(predicate)
			.fetch();
	}

	@Override
	public List<BookStoreContentResponse> findAll(Predicate predicate, OrderSpecification orderSpec) {
		JPAQuery<BookStoreContentResponse> query = factory.select(new QBookStoreContentResponse(book, bookStoreBook))
			.from(bookStore)
			.join(bookStore.storedBooks, bookStoreBook)
			.join(book).on(book.id.eq(bookStoreBook.bookId))
			.where(predicate);

		orderSpec.orderBy(query);
		return query.fetch();
	}
}

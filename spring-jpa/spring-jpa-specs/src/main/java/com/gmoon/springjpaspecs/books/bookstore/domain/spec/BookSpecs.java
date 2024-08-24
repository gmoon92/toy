package com.gmoon.springjpaspecs.books.bookstore.domain.spec;

import java.time.Duration;
import java.time.Instant;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Predicate;

import com.gmoon.springjpaspecs.books.bookstore.domain.BookStoreBook;
import com.gmoon.springjpaspecs.books.bookstore.domain.QBookStoreBook;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;

public class BookSpecs {

	@QueryDelegate(BookStoreBook.class)
	public static Predicate isDisplayed(QBookStoreBook bookStoreBook) {
		return new BooleanBuilder(bookStoreBook.status.eq(BookStatus.DISPLAY))
			 .and(availableSale(bookStoreBook));
	}

	@QueryDelegate(BookStoreBook.class)
	public static Predicate availableSale(QBookStoreBook bookStoreBook) {
		return bookStoreBook.quantity.value.goe(0);
	}

	@QueryDelegate(BookStoreBook.class)
	public static Predicate isHidden(QBookStoreBook bookStoreBook) {
		return bookStoreBook.status.eq(BookStatus.HIDDEN);
	}

	@QueryDelegate(BookStoreBook.class)
	public static Predicate isDiscount(QBookStoreBook bookStoreBook) {
		return new BooleanBuilder(isDisplayed(bookStoreBook))
			 .and(isNewBook(bookStoreBook));
	}

	@QueryDelegate(BookStoreBook.class)
	public static Predicate isNewBook(QBookStoreBook bookStoreBook) {
		Instant now = Instant.now();
		return bookStoreBook.createdAt.after(now.plus(Duration.ofDays(-7)));
	}
}

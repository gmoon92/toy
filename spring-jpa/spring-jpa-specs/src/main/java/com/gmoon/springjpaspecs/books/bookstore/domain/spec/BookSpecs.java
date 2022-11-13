package com.gmoon.springjpaspecs.books.bookstore.domain.spec;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.gmoon.springjpaspecs.books.book.domain.QBook;
import com.gmoon.springjpaspecs.books.bookstore.domain.QBookStoreBook;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus;
import com.gmoon.springjpaspecs.global.specs.conditional.CompositeSpecification;
import com.gmoon.springjpaspecs.global.specs.conditional.NotSpecification;
import com.querydsl.core.types.Predicate;

public class BookSpecs {

	@SuppressWarnings("unchecked")
	public static Predicate display(QBookStoreBook root) {
		return CompositeSpecification.<QBookStoreBook>create()
			.and(
				entityPath -> entityPath.status.eq(BookStatus.DISPLAY),
				entityPath -> entityPath.quantity.value.goe(0)
			)
			.isSatisfiedBy(root);
	}

	@SuppressWarnings("unchecked")
	public static Predicate hidden(QBookStoreBook root) {
		return CompositeSpecification.<QBookStoreBook>create()
			.and(entityPath -> entityPath.status.eq(BookStatus.HIDDEN))
			.or(NotSpecification.of(BookSpecs::display))
			.isSatisfiedBy(root);
	}

	@SuppressWarnings("unchecked")
	public static Predicate discountBook(QBook root) {
		LocalDateTime now = LocalDateTime.now();

		return CompositeSpecification.<QBook>create()
			.and(
				entityPath -> entityPath.publicationDate.between(now.plus(40, ChronoUnit.YEARS), now)
			)
			.isSatisfiedBy(root);
	}

}

package com.gmoon.springjpaspecs.books;

import java.math.BigDecimal;

import com.gmoon.springjpaspecs.books.book.domain.Book;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookPrice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Fixtures {

	public static Book book(String bookName, BigDecimal bookPrice) {
		return Book.builder()
			 .name(new BookName(bookName))
			 .price(new BookPrice(bookPrice))
			 .build();
	}
}

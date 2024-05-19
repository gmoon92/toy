package com.gmoon.springjpaspecs.books.bookstore.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.springjpaspecs.books.book.domain.Book;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookPrice;
import com.gmoon.springjpaspecs.books.bookstore.domain.BookStoreBook;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;

import lombok.Getter;

@Getter
public class BookStoreContentResponse {

	private final String bookId;
	private final String isbn;
	private final BookType bookType;
	private final BookName bookName;
	private final BookPrice bookPrice;
	private final BookQuantity bookQuantity;
	private final LocalDateTime createdDateTime;

	@QueryProjection
	public BookStoreContentResponse(Book book, BookStoreBook bookStoreBook) {
		this.bookId = book.getId();
		this.isbn = book.getIsbn();
		this.bookType = bookStoreBook.getType();
		this.bookName = book.getName();
		this.bookPrice = book.getPrice();
		this.bookQuantity = bookStoreBook.getQuantity();
		this.createdDateTime = bookStoreBook.getCreatedDate();
	}
}

package com.gmoon.springjpaspecs.books.bookstore.dto;

import java.time.LocalDateTime;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.book.domain.vo.BookPrice;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookQuantity;
import com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BookStoreContentResponse {

	private final String bookId;
	private final String isbn;
	private final BookType bookType;
	private final BookName bookName;
	private final BookPrice bookPrice;
	private final BookQuantity bookQuantity;
	private final LocalDateTime createdDateTime;
}

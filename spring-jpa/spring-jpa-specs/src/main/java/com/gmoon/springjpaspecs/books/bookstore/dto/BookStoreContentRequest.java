package com.gmoon.springjpaspecs.books.bookstore.dto;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;
import com.querydsl.core.types.Order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BookStoreContentRequest {

	private final BookName bookName;
	private final SortTargetType sortTargetType;
	private final Order order;
}

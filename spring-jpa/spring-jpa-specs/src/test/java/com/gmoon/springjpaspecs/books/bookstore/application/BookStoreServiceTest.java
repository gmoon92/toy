package com.gmoon.springjpaspecs.books.bookstore.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springjpaspecs.books.book.domain.vo.BookName;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentRequest;
import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentResponse;
import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;
import com.querydsl.core.types.Order;

@SpringBootTest
class BookStoreServiceTest {

	@Autowired
	private BookStoreService service;

	@Test
	void findAll() {
		BookStoreContentRequest.Search search = new BookStoreContentRequest.Search();
		search.setName("START! DDD");

		BookStoreContentRequest request = BookStoreContentRequest.builder()
			.sortTargetType(SortTargetType.NAME)
			.order(Order.DESC)
			.search(search)
			.build();

		assertThat(service.findAll(request))
			.map(BookStoreContentResponse::getBookName)
			.containsOnly(new BookName("START! DDD"));
	}
}

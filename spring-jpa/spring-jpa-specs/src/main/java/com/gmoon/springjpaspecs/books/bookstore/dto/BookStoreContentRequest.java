package com.gmoon.springjpaspecs.books.bookstore.dto;

import com.querydsl.core.types.Order;

import com.gmoon.springjpaspecs.books.bookstore.model.SortTargetType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookStoreContentRequest {

	private Search search;
	private SortTargetType sortTargetType;
	private Order order;

	@Builder
	private BookStoreContentRequest(SortTargetType sortTargetType, Order order, Search search) {
		this.sortTargetType = sortTargetType;
		this.order = order;
		this.search = search;
	}

	@Getter
	@Setter
	public static class Search {

		private boolean isDiscountBook;
		private String name;
	}
}

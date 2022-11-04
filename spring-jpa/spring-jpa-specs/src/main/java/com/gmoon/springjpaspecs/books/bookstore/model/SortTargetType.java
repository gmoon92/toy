package com.gmoon.springjpaspecs.books.bookstore.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SortTargetType {

	NAME("name"),
	PRICE("price"),
	DATE("createdDate");

	private final String value;
}

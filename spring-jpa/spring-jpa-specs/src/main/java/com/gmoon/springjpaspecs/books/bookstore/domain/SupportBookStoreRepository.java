package com.gmoon.springjpaspecs.books.bookstore.domain;

import java.util.List;

import com.querydsl.core.types.Predicate;

import com.gmoon.springjpaspecs.books.bookstore.dto.BookStoreContentResponse;
import com.gmoon.springjpaspecs.global.specs.orderby.OrderSpecification;

public interface SupportBookStoreRepository {

	List<BookStoreContentResponse> findAll(OrderSpecification orderSpec);

	List<BookStore> findAll(Predicate predicate);

	List<BookStoreContentResponse> findAll(Predicate predicate, OrderSpecification orderSpec);
}

package com.gmoon.springjpaspecs.books.bookstore.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface JpaBookStoreRepository extends JpaRepository<BookStore, String>,
	 SupportBookStoreRepository,
	 QuerydslPredicateExecutor<BookStore> {

}

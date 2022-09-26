package com.gmoon.springjpaspecs.books.book.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookRepository extends BookRepository<Book, String>,
	JpaRepository<Book, String> {
}

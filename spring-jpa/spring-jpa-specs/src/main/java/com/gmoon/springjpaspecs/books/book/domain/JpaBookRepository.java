package com.gmoon.springjpaspecs.books.book.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookRepository extends BookRepository<Book, UUID>,
	JpaRepository<Book, UUID> {
}

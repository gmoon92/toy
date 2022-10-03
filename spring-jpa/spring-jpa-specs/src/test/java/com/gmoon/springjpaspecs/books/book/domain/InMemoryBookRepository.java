package com.gmoon.springjpaspecs.books.book.domain;

import java.util.UUID;

import com.gmoon.springjpaspecs.global.application.SupportInMemoryRepository;

public class InMemoryBookRepository extends SupportInMemoryRepository<Book, UUID>
	implements BookRepository<Book, UUID> {

}

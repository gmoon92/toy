package com.gmoon.springjpaspecs.books.book.domain;

import java.util.List;
import java.util.Optional;

public interface BookRepository<Book, UUID> {

	Book save(Book book);

	List<Book> findAll();

	Optional<Book> findById(UUID id);

	List<Book> findAllById(Iterable<UUID> ids);
}

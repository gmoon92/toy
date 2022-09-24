package com.gmoon.springjpaspecs.books.book.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

	Book save(Book book);

	List<Book> findAll();

	Optional<Book> findById(final UUID id);

	List<Book> findAllById(final List<UUID> ids);
}

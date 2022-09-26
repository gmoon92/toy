package com.gmoon.springjpaspecs.books.book.domain;

import java.util.List;
import java.util.Optional;

public interface BookRepository<Entity, ID> {

	Entity save(Entity entity);

	List<Entity> findAll();

	Optional<Entity> findById(ID id);

	List<Entity> findAllById(Iterable<ID> ids);
}

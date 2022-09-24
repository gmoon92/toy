package com.gmoon.springjpaspecs.books.domain;

import com.gmoon.springjpaspecs.global.application.BaseInMemoryRepository;
import java.util.UUID;

public class InMemoryBookRepository extends BaseInMemoryRepository<Book, UUID>
	implements BookRepository {

}

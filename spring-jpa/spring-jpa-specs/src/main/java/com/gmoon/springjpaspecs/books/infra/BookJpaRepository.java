package com.gmoon.springjpaspecs.books.infra;

import com.gmoon.springjpaspecs.books.domain.Book;
import com.gmoon.springjpaspecs.books.domain.BookRepository;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookJpaRepository extends BookRepository, JpaRepository<Book, Serializable> {
}

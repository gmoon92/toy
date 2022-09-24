package com.gmoon.springjpaspecs.books.book.infra;

import com.gmoon.springjpaspecs.books.book.domain.Book;
import com.gmoon.springjpaspecs.books.book.domain.BookRepository;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookJpaRepository extends BookRepository, JpaRepository<Book, Serializable> {
}

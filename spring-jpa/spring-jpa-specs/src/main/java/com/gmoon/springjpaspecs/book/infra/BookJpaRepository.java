package com.gmoon.springjpaspecs.book.infra;

import com.gmoon.springjpaspecs.book.domain.BookRepository;
import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springjpaspecs.book.domain.Book;

public interface BookJpaRepository extends BookRepository, JpaRepository<Book, Serializable> {
}

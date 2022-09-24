package com.gmoon.springjpaspecs.book.infra;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springjpaspecs.book.domain.Book;

public interface BookJpaRepository extends JpaRepository<Book, Serializable> {
}

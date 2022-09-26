package com.gmoon.springjpaspecs.books.bookstore.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookStoreRepository extends JpaRepository<BookStore, String> {

}

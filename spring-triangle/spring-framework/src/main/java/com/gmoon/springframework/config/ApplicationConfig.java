package com.gmoon.springframework.config;

import com.gmoon.springframework.book.BookRepository;
import com.gmoon.springframework.book.BookService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

  @Bean
  public BookRepository bookRepository() {
    return new BookRepository();
  }

  @Bean
  public BookService bookService(BookRepository bookRepository) {
    return new BookService(bookRepository);
  }
}

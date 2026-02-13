package com.gmoon.springjpaspecs.books.bookstore.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.gmoon.springjpaspecs.books.bookstore.dto.QBookStoreContentResponse is a Querydsl Projection type for BookStoreContentResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QBookStoreContentResponse extends ConstructorExpression<BookStoreContentResponse> {

    private static final long serialVersionUID = 1823336444L;

    public QBookStoreContentResponse(com.querydsl.core.types.Expression<? extends com.gmoon.springjpaspecs.books.book.domain.Book> book, com.querydsl.core.types.Expression<? extends com.gmoon.springjpaspecs.books.bookstore.domain.BookStoreBook> bookStoreBook) {
        super(BookStoreContentResponse.class, new Class<?>[]{com.gmoon.springjpaspecs.books.book.domain.Book.class, com.gmoon.springjpaspecs.books.bookstore.domain.BookStoreBook.class}, book, bookStoreBook);
    }

}


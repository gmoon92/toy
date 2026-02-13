package com.gmoon.springjpaspecs.books.bookstore.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookStore is a Querydsl query type for BookStore
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookStore extends EntityPathBase<BookStore> {

    private static final long serialVersionUID = 29657241L;

    public static final QBookStore bookStore = new QBookStore("bookStore");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final ListPath<BookStoreBook, QBookStoreBook> storedBooks = this.<BookStoreBook, QBookStoreBook>createList("storedBooks", BookStoreBook.class, QBookStoreBook.class, PathInits.DIRECT2);

    public QBookStore(String variable) {
        super(BookStore.class, forVariable(variable));
    }

    public QBookStore(Path<? extends BookStore> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookStore(PathMetadata metadata) {
        super(BookStore.class, metadata);
    }

}


package com.gmoon.springjpaspecs.books.book.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBook is a Querydsl query type for Book
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBook extends EntityPathBase<Book> {

    private static final long serialVersionUID = -593260797L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBook book = new QBook("book");

    public final StringPath id = createString("id");

    public final StringPath isbn = createString("isbn");

    public final com.gmoon.springjpaspecs.books.book.domain.vo.QBookName name;

    public final com.gmoon.springjpaspecs.books.book.domain.vo.QBookPrice price;

    public final DateTimePath<java.time.Instant> publicationAt = createDateTime("publicationAt", java.time.Instant.class);

    public QBook(String variable) {
        this(Book.class, forVariable(variable), INITS);
    }

    public QBook(Path<? extends Book> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBook(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBook(PathMetadata metadata, PathInits inits) {
        this(Book.class, metadata, inits);
    }

    public QBook(Class<? extends Book> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.name = inits.isInitialized("name") ? new com.gmoon.springjpaspecs.books.book.domain.vo.QBookName(forProperty("name")) : null;
        this.price = inits.isInitialized("price") ? new com.gmoon.springjpaspecs.books.book.domain.vo.QBookPrice(forProperty("price")) : null;
    }

}


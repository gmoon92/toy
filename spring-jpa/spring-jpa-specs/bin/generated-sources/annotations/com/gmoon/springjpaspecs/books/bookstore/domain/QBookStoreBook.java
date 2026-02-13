package com.gmoon.springjpaspecs.books.bookstore.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.gmoon.springjpaspecs.books.bookstore.domain.spec.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookStoreBook is a Querydsl query type for BookStoreBook
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookStoreBook extends EntityPathBase<BookStoreBook> {

    private static final long serialVersionUID = 80495394L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookStoreBook bookStoreBook = new QBookStoreBook("bookStoreBook");

    public final com.gmoon.springjpaspecs.global.domain.QAuditedEntityObject _super = new com.gmoon.springjpaspecs.global.domain.QAuditedEntityObject(this);

    public final StringPath bookId = createString("bookId");

    public final StringPath bookName = createString("bookName");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final StringPath id = createString("id");

    //inherited
    public final DateTimePath<java.time.Instant> modifiedAt = _super.modifiedAt;

    public final com.gmoon.springjpaspecs.books.bookstore.domain.vo.QBookQuantity quantity;

    public final EnumPath<com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus> status = createEnum("status", com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookStatus.class);

    public final EnumPath<com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType> type = createEnum("type", com.gmoon.springjpaspecs.books.bookstore.domain.vo.BookType.class);

    public QBookStoreBook(String variable) {
        this(BookStoreBook.class, forVariable(variable), INITS);
    }

    public QBookStoreBook(Path<? extends BookStoreBook> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookStoreBook(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookStoreBook(PathMetadata metadata, PathInits inits) {
        this(BookStoreBook.class, metadata, inits);
    }

    public QBookStoreBook(Class<? extends BookStoreBook> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.quantity = inits.isInitialized("quantity") ? new com.gmoon.springjpaspecs.books.bookstore.domain.vo.QBookQuantity(forProperty("quantity")) : null;
    }

    public com.querydsl.core.types.Predicate availableSale() {
        return BookSpecs.availableSale(this);
    }

    public com.querydsl.core.types.Predicate isDisplayed() {
        return BookSpecs.isDisplayed(this);
    }

    public com.querydsl.core.types.Predicate isDiscount() {
        return BookSpecs.isDiscount(this);
    }

    public com.querydsl.core.types.Predicate isNewBook() {
        return BookSpecs.isNewBook(this);
    }

    public com.querydsl.core.types.Predicate isHidden() {
        return BookSpecs.isHidden(this);
    }

}


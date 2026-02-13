package com.gmoon.springjpaspecs.books.book.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookName is a Querydsl query type for BookName
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QBookName extends BeanPath<BookName> {

    private static final long serialVersionUID = -553949297L;

    public static final QBookName bookName = new QBookName("bookName");

    public final StringPath value = createString("value");

    public QBookName(String variable) {
        super(BookName.class, forVariable(variable));
    }

    public QBookName(Path<? extends BookName> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookName(PathMetadata metadata) {
        super(BookName.class, metadata);
    }

}


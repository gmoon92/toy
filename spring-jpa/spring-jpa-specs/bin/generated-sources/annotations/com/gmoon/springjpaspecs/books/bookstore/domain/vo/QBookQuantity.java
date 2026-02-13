package com.gmoon.springjpaspecs.books.bookstore.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookQuantity is a Querydsl query type for BookQuantity
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QBookQuantity extends BeanPath<BookQuantity> {

    private static final long serialVersionUID = -899978166L;

    public static final QBookQuantity bookQuantity = new QBookQuantity("bookQuantity");

    public final NumberPath<Integer> value = createNumber("value", Integer.class);

    public QBookQuantity(String variable) {
        super(BookQuantity.class, forVariable(variable));
    }

    public QBookQuantity(Path<? extends BookQuantity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookQuantity(PathMetadata metadata) {
        super(BookQuantity.class, metadata);
    }

}


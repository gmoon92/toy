package com.gmoon.springjpaspecs.books.book.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBookPrice is a Querydsl query type for BookPrice
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QBookPrice extends BeanPath<BookPrice> {

    private static final long serialVersionUID = 9790661L;

    public static final QBookPrice bookPrice = new QBookPrice("bookPrice");

    public final NumberPath<java.math.BigDecimal> value = createNumber("value", java.math.BigDecimal.class);

    public QBookPrice(String variable) {
        super(BookPrice.class, forVariable(variable));
    }

    public QBookPrice(Path<? extends BookPrice> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBookPrice(PathMetadata metadata) {
        super(BookPrice.class, metadata);
    }

}


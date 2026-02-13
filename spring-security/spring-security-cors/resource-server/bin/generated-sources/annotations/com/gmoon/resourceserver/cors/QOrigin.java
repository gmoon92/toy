package com.gmoon.resourceserver.cors;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrigin is a Querydsl query type for Origin
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrigin extends BeanPath<Origin> {

    private static final long serialVersionUID = -114223203L;

    public static final QOrigin origin = new QOrigin("origin");

    public final StringPath host = createString("host");

    public final NumberPath<Integer> port = createNumber("port", Integer.class);

    public final StringPath schema = createString("schema");

    public QOrigin(String variable) {
        super(Origin.class, forVariable(variable));
    }

    public QOrigin(Path<? extends Origin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrigin(PathMetadata metadata) {
        super(Origin.class, metadata);
    }

}


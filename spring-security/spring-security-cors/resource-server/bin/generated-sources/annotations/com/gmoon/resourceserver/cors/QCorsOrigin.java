package com.gmoon.resourceserver.cors;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCorsOrigin is a Querydsl query type for CorsOrigin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCorsOrigin extends EntityPathBase<CorsOrigin> {

    private static final long serialVersionUID = 183790122L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCorsOrigin corsOrigin = new QCorsOrigin("corsOrigin");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrigin origin;

    public QCorsOrigin(String variable) {
        this(CorsOrigin.class, forVariable(variable), INITS);
    }

    public QCorsOrigin(Path<? extends CorsOrigin> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCorsOrigin(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCorsOrigin(PathMetadata metadata, PathInits inits) {
        this(CorsOrigin.class, metadata, inits);
    }

    public QCorsOrigin(Class<? extends CorsOrigin> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.origin = inits.isInitialized("origin") ? new QOrigin(forProperty("origin")) : null;
    }

}


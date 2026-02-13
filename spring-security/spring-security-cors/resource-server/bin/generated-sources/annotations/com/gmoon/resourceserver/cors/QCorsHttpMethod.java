package com.gmoon.resourceserver.cors;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCorsHttpMethod is a Querydsl query type for CorsHttpMethod
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCorsHttpMethod extends EntityPathBase<CorsHttpMethod> {

    private static final long serialVersionUID = -432351091L;

    public static final QCorsHttpMethod corsHttpMethod = new QCorsHttpMethod("corsHttpMethod");

    public final BooleanPath enabled = createBoolean("enabled");

    public final StringPath httpMethod = createString("httpMethod");

    public QCorsHttpMethod(String variable) {
        super(CorsHttpMethod.class, forVariable(variable));
    }

    public QCorsHttpMethod(Path<? extends CorsHttpMethod> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCorsHttpMethod(PathMetadata metadata) {
        super(CorsHttpMethod.class, metadata);
    }

}


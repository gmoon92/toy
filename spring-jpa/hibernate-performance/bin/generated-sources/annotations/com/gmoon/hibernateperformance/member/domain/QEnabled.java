package com.gmoon.hibernateperformance.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEnabled is a Querydsl query type for Enabled
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QEnabled extends BeanPath<Enabled> {

    private static final long serialVersionUID = -1595232498L;

    public static final QEnabled enabled1 = new QEnabled("enabled1");

    public final BooleanPath enabled = createBoolean("enabled");

    public final DateTimePath<java.time.Instant> enabledAt = createDateTime("enabledAt", java.time.Instant.class);

    public QEnabled(String variable) {
        super(Enabled.class, forVariable(variable));
    }

    public QEnabled(Path<? extends Enabled> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEnabled(PathMetadata metadata) {
        super(Enabled.class, metadata);
    }

}


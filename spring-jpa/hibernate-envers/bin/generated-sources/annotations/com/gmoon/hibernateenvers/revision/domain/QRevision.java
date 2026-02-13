package com.gmoon.hibernateenvers.revision.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRevision is a Querydsl query type for Revision
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRevision extends EntityPathBase<Revision> {

    private static final long serialVersionUID = -653446776L;

    public static final QRevision revision = new QRevision("revision");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final SetPath<RevisionHistory, QRevisionHistory> details = this.<RevisionHistory, QRevisionHistory>createSet("details", RevisionHistory.class, QRevisionHistory.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath updatedBy = createString("updatedBy");

    public final StringPath updatedByUsername = createString("updatedByUsername");

    public QRevision(String variable) {
        super(Revision.class, forVariable(variable));
    }

    public QRevision(Path<? extends Revision> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRevision(PathMetadata metadata) {
        super(Revision.class, metadata);
    }

}


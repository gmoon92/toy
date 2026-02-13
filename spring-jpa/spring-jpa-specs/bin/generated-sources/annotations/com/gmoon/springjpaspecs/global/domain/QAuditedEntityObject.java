package com.gmoon.springjpaspecs.global.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuditedEntityObject is a Querydsl query type for AuditedEntityObject
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAuditedEntityObject extends EntityPathBase<AuditedEntityObject<? extends java.io.Serializable>> {

    private static final long serialVersionUID = -429745588L;

    public static final QAuditedEntityObject auditedEntityObject = new QAuditedEntityObject("auditedEntityObject");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> modifiedAt = createDateTime("modifiedAt", java.time.Instant.class);

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QAuditedEntityObject(String variable) {
        super((Class) AuditedEntityObject.class, forVariable(variable));
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QAuditedEntityObject(Path<? extends AuditedEntityObject> path) {
        super((Class) path.getType(), path.getMetadata());
    }

    @SuppressWarnings({"all", "rawtypes", "unchecked"})
    public QAuditedEntityObject(PathMetadata metadata) {
        super((Class) AuditedEntityObject.class, metadata);
    }

}


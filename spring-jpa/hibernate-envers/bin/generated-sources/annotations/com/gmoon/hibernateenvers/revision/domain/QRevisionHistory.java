package com.gmoon.hibernateenvers.revision.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRevisionHistory is a Querydsl query type for RevisionHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRevisionHistory extends EntityPathBase<RevisionHistory> {

    private static final long serialVersionUID = 838992364L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRevisionHistory revisionHistory = new QRevisionHistory("revisionHistory");

    public final ArrayPath<byte[], Byte> entityId = createArray("entityId", byte[].class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRevision revision;

    public final EnumPath<com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus> status = createEnum("status", com.gmoon.hibernateenvers.revision.domain.vo.RevisionStatus.class);

    public final EnumPath<com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget> target = createEnum("target", com.gmoon.hibernateenvers.revision.domain.vo.RevisionTarget.class);

    public final com.gmoon.hibernateenvers.member.domain.QMember targetMember;

    public final StringPath targetMemberName = createString("targetMemberName");

    public final EnumPath<org.hibernate.envers.RevisionType> type = createEnum("type", org.hibernate.envers.RevisionType.class);

    public QRevisionHistory(String variable) {
        this(RevisionHistory.class, forVariable(variable), INITS);
    }

    public QRevisionHistory(Path<? extends RevisionHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRevisionHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRevisionHistory(PathMetadata metadata, PathInits inits) {
        this(RevisionHistory.class, metadata, inits);
    }

    public QRevisionHistory(Class<? extends RevisionHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.revision = inits.isInitialized("revision") ? new QRevision(forProperty("revision")) : null;
        this.targetMember = inits.isInitialized("targetMember") ? new com.gmoon.hibernateenvers.member.domain.QMember(forProperty("targetMember")) : null;
    }

}


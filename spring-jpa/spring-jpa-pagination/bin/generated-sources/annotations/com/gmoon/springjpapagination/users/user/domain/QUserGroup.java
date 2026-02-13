package com.gmoon.springjpapagination.users.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.gmoon.springjpapagination.global.domain.expressions.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserGroup is a Querydsl query type for UserGroup
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserGroup extends EntityPathBase<UserGroup> {

    private static final long serialVersionUID = 1086327342L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserGroup userGroup = new QUserGroup("userGroup");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final QUserGroup parentGroup;

    public QUserGroup(String variable) {
        this(UserGroup.class, forVariable(variable), INITS);
    }

    public QUserGroup(Path<? extends UserGroup> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserGroup(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserGroup(PathMetadata metadata, PathInits inits) {
        this(UserGroup.class, metadata, inits);
    }

    public QUserGroup(Class<? extends UserGroup> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parentGroup = inits.isInitialized("parentGroup") ? new QUserGroup(forProperty("parentGroup"), inits.get("parentGroup")) : null;
    }

    public com.querydsl.core.types.Predicate likeName(String keyword) {
        return UserGroupExpressions.likeName(this, keyword);
    }

    public com.querydsl.core.types.Predicate assignedGroup(String groupId) {
        return UserGroupExpressions.assignedGroup(this, groupId);
    }

}


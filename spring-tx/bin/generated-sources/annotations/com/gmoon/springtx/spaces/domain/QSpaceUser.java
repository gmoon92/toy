package com.gmoon.springtx.spaces.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSpaceUser is a Querydsl query type for SpaceUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSpaceUser extends EntityPathBase<SpaceUser> {

    private static final long serialVersionUID = -4349570L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSpaceUser spaceUser = new QSpaceUser("spaceUser");

    public final StringPath id = createString("id");

    public final QSpace space;

    public final StringPath userId = createString("userId");

    public QSpaceUser(String variable) {
        this(SpaceUser.class, forVariable(variable), INITS);
    }

    public QSpaceUser(Path<? extends SpaceUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSpaceUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSpaceUser(PathMetadata metadata, PathInits inits) {
        this(SpaceUser.class, metadata, inits);
    }

    public QSpaceUser(Class<? extends SpaceUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.space = inits.isInitialized("space") ? new QSpace(forProperty("space")) : null;
    }

}


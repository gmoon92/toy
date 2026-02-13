package com.gmoon.springtx.spaces.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSpace is a Querydsl query type for Space
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSpace extends EntityPathBase<Space> {

    private static final long serialVersionUID = 1841496851L;

    public static final QSpace space = new QSpace("space");

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final ListPath<SpaceUser, QSpaceUser> spaceUsers = this.<SpaceUser, QSpaceUser>createList("spaceUsers", SpaceUser.class, QSpaceUser.class, PathInits.DIRECT2);

    public QSpace(String variable) {
        super(Space.class, forVariable(variable));
    }

    public QSpace(Path<? extends Space> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSpace(PathMetadata metadata) {
        super(Space.class, metadata);
    }

}


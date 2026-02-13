package com.gmoon.querydslprojections.movies.users.user.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDirector is a Querydsl query type for Director
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDirector extends EntityPathBase<Director> {

    private static final long serialVersionUID = -748289678L;

    public static final QDirector director = new QDirector("director");

    public final QUser _super = new QUser(this);

    //inherited
    public final StringPath id = _super.id;

    //inherited
    public final StringPath name = _super.name;

    public QDirector(String variable) {
        super(Director.class, forVariable(variable));
    }

    public QDirector(Path<? extends Director> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDirector(PathMetadata metadata) {
        super(Director.class, metadata);
    }

}


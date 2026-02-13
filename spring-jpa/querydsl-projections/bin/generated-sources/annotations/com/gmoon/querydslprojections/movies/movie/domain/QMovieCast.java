package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMovieCast is a Querydsl query type for MovieCast
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMovieCast extends EntityPathBase<MovieCast> {

    private static final long serialVersionUID = 2122912028L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMovieCast movieCast = new QMovieCast("movieCast");

    public final com.gmoon.querydslprojections.movies.users.user.domain.QActor actor;

    public final QMovieCast_Id id;

    public final QMovie movie;

    public QMovieCast(String variable) {
        this(MovieCast.class, forVariable(variable), INITS);
    }

    public QMovieCast(Path<? extends MovieCast> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMovieCast(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMovieCast(PathMetadata metadata, PathInits inits) {
        this(MovieCast.class, metadata, inits);
    }

    public QMovieCast(Class<? extends MovieCast> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actor = inits.isInitialized("actor") ? new com.gmoon.querydslprojections.movies.users.user.domain.QActor(forProperty("actor")) : null;
        this.id = inits.isInitialized("id") ? new QMovieCast_Id(forProperty("id")) : null;
        this.movie = inits.isInitialized("movie") ? new QMovie(forProperty("movie"), inits.get("movie")) : null;
    }

}


package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMovieDirector is a Querydsl query type for MovieDirector
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMovieDirector extends EntityPathBase<MovieDirector> {

    private static final long serialVersionUID = 266121961L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMovieDirector movieDirector = new QMovieDirector("movieDirector");

    public final com.gmoon.querydslprojections.movies.users.user.domain.QDirector director;

    public final QMovieDirector_Id id;

    public final QMovie movie;

    public QMovieDirector(String variable) {
        this(MovieDirector.class, forVariable(variable), INITS);
    }

    public QMovieDirector(Path<? extends MovieDirector> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMovieDirector(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMovieDirector(PathMetadata metadata, PathInits inits) {
        this(MovieDirector.class, metadata, inits);
    }

    public QMovieDirector(Class<? extends MovieDirector> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.director = inits.isInitialized("director") ? new com.gmoon.querydslprojections.movies.users.user.domain.QDirector(forProperty("director")) : null;
        this.id = inits.isInitialized("id") ? new QMovieDirector_Id(forProperty("id")) : null;
        this.movie = inits.isInitialized("movie") ? new QMovie(forProperty("movie"), inits.get("movie")) : null;
    }

}


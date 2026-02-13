package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMovie is a Querydsl query type for Movie
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMovie extends EntityPathBase<Movie> {

    private static final long serialVersionUID = -284063683L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMovie movie = new QMovie("movie");

    public final SetPath<MovieCast, QMovieCast> castMembers = this.<MovieCast, QMovieCast>createSet("castMembers", MovieCast.class, QMovieCast.class, PathInits.DIRECT2);

    public final QMovieDirector director;

    public final EnumPath<FilmRating> filmRating = createEnum("filmRating", FilmRating.class);

    public final EnumPath<MovieGenre> genre = createEnum("genre", MovieGenre.class);

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final QMovieReleaseTime releaseTime;

    public final NumberPath<Long> runningMinutes = createNumber("runningMinutes", Long.class);

    public QMovie(String variable) {
        this(Movie.class, forVariable(variable), INITS);
    }

    public QMovie(Path<? extends Movie> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMovie(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMovie(PathMetadata metadata, PathInits inits) {
        this(Movie.class, metadata, inits);
    }

    public QMovie(Class<? extends Movie> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.director = inits.isInitialized("director") ? new QMovieDirector(forProperty("director"), inits.get("director")) : null;
        this.releaseTime = inits.isInitialized("releaseTime") ? new QMovieReleaseTime(forProperty("releaseTime")) : null;
    }

}


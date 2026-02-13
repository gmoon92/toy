package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMovieCast_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QMovieCast_Id extends BeanPath<MovieCast.Id> {

    private static final long serialVersionUID = 278839117L;

    public static final QMovieCast_Id id = new QMovieCast_Id("id");

    public final StringPath actorId = createString("actorId");

    public final StringPath movieId = createString("movieId");

    public QMovieCast_Id(String variable) {
        super(MovieCast.Id.class, forVariable(variable));
    }

    public QMovieCast_Id(Path<? extends MovieCast.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMovieCast_Id(PathMetadata metadata) {
        super(MovieCast.Id.class, metadata);
    }

}


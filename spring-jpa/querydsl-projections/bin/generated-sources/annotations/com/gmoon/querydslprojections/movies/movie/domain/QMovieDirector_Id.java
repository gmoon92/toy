package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMovieDirector_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QMovieDirector_Id extends BeanPath<MovieDirector.Id> {

    private static final long serialVersionUID = -470241696L;

    public static final QMovieDirector_Id id = new QMovieDirector_Id("id");

    public final StringPath directorId = createString("directorId");

    public final StringPath movieId = createString("movieId");

    public QMovieDirector_Id(String variable) {
        super(MovieDirector.Id.class, forVariable(variable));
    }

    public QMovieDirector_Id(Path<? extends MovieDirector.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMovieDirector_Id(PathMetadata metadata) {
        super(MovieDirector.Id.class, metadata);
    }

}


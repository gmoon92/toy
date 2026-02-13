package com.gmoon.querydslprojections.movies.movie.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMovieReleaseTime is a Querydsl query type for MovieReleaseTime
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QMovieReleaseTime extends BeanPath<MovieReleaseTime> {

    private static final long serialVersionUID = 1369381623L;

    public static final QMovieReleaseTime movieReleaseTime = new QMovieReleaseTime("movieReleaseTime");

    public final NumberPath<Integer> dayOfMonth = createNumber("dayOfMonth", Integer.class);

    public final NumberPath<Integer> hour = createNumber("hour", Integer.class);

    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    public final DateTimePath<java.time.Instant> value = createDateTime("value", java.time.Instant.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QMovieReleaseTime(String variable) {
        super(MovieReleaseTime.class, forVariable(variable));
    }

    public QMovieReleaseTime(Path<? extends MovieReleaseTime> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMovieReleaseTime(PathMetadata metadata) {
        super(MovieReleaseTime.class, metadata);
    }

}


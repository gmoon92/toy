package com.gmoon.springtx.favorites.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFavorite_Id is a Querydsl query type for Id
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QFavorite_Id extends BeanPath<Favorite.Id> {

    private static final long serialVersionUID = 283376260L;

    public static final QFavorite_Id id = new QFavorite_Id("id");

    public final EnumPath<FavoriteType> type = createEnum("type", FavoriteType.class);

    public final StringPath userId = createString("userId");

    public QFavorite_Id(String variable) {
        super(Favorite.Id.class, forVariable(variable));
    }

    public QFavorite_Id(Path<? extends Favorite.Id> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFavorite_Id(PathMetadata metadata) {
        super(Favorite.Id.class, metadata);
    }

}


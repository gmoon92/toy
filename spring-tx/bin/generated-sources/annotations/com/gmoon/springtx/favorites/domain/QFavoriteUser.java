package com.gmoon.springtx.favorites.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFavoriteUser is a Querydsl query type for FavoriteUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoriteUser extends EntityPathBase<FavoriteUser> {

    private static final long serialVersionUID = 195931824L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFavoriteUser favoriteUser = new QFavoriteUser("favoriteUser");

    public final QFavorite favorite;

    public final StringPath id = createString("id");

    public final StringPath userId = createString("userId");

    public QFavoriteUser(String variable) {
        this(FavoriteUser.class, forVariable(variable), INITS);
    }

    public QFavoriteUser(Path<? extends FavoriteUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFavoriteUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFavoriteUser(PathMetadata metadata, PathInits inits) {
        this(FavoriteUser.class, metadata, inits);
    }

    public QFavoriteUser(Class<? extends FavoriteUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.favorite = inits.isInitialized("favorite") ? new QFavorite(forProperty("favorite"), inits.get("favorite")) : null;
    }

}


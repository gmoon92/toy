package com.gmoon.springtx.favorites.domain;

import static com.gmoon.springtx.favorites.domain.QFavorite.*;
import static com.gmoon.springtx.favorites.domain.QFavoriteUser.*;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FavoriteRepositoryQueryImpl implements FavoriteRepositoryQuery{

	private final JPAQueryFactory factory;

	@Override
	public void delete(String userId) {
		factory.delete(favoriteUser)
			.where(favoriteUser.userId.eq(userId))
			.execute();

		factory.delete(favoriteUser)
			.where(favoriteUser.favorite.id.userId.eq(userId))
			.execute();

		factory.delete(favorite)
			.where(favorite.id.userId.eq(userId))
			.execute();
	}
}

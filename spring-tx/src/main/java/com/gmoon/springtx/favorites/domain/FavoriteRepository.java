package com.gmoon.springtx.favorites.domain;

import org.springframework.data.repository.CrudRepository;

public interface FavoriteRepository extends CrudRepository<Favorite, Favorite.Id>,
	FavoriteRepositoryQuery {

}

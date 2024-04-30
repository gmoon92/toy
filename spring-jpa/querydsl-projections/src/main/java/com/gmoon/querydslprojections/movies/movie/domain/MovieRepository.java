package com.gmoon.querydslprojections.movies.movie.domain;

import java.io.Serializable;
import java.util.List;

public interface MovieRepository<T extends Movie, ID extends Serializable> {

	T save(T movie);

	T getReferenceById(ID id);

	List<T> findAll();
}

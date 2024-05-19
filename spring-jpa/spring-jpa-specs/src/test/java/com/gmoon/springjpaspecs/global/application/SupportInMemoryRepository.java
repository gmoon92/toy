package com.gmoon.springjpaspecs.global.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.gmoon.springjpaspecs.global.domain.EntityObject;

public abstract class SupportInMemoryRepository<T extends EntityObject, ID extends Serializable> {

	private final Map<ID, T> CACHE = new HashMap<>();

	public T save(T entity) {
		CACHE.put((ID)entity.getId(), entity);
		return entity;
	}

	public List<T> saveAll(Iterable<T> entities) {
		for (T entity : entities) {
			save(entity);
		}

		return new ArrayList<>(CACHE.values());
	}

	public Optional<T> findById(ID id) {
		T entity = CACHE.get(id);
		return Optional.ofNullable(entity);
	}

	public boolean existsById(ID id) {
		return !Objects.isNull(CACHE.get(id));
	}

	public long count() {
		return CACHE.size();
	}

	public List<T> findAll() {
		return new ArrayList<>(CACHE.values());
	}

	public List<T> findAllById(Iterable<ID> ids) {
		List<T> result = new ArrayList<>();
		for (ID id : ids) {
			if (existsById(id)) {
				result.add(CACHE.get(id));
			}
		}

		return result;
	}

	public T getReferenceById(ID id) {
		return CACHE.get(id);
	}

	public void deleteById(ID id) {
		CACHE.remove(id);
	}

	public void delete(T entity) {
		Serializable id = entity.getId();
		CACHE.remove(id);
	}

	public void deleteAllById(Iterable<? extends ID> ids) {
		for (ID id : ids) {
			deleteById(id);
		}
	}

	public void deleteAll(Iterable<? extends T> entities) {
		for (T entity : entities) {
			CACHE.remove(entities);
		}
	}

	public void deleteAll() {
		CACHE.clear();
	}
}

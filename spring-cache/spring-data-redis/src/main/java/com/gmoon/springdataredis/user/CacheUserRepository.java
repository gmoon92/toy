package com.gmoon.springdataredis.user;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

public interface CacheUserRepository extends CrudRepository<CacheUser, Long> {
	@Cacheable(value = CacheUser.KEY, unless = "#result==null")
	Optional<CacheUser> findByUsername(String username);
}

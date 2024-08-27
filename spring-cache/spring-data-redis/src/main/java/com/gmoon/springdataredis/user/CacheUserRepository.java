package com.gmoon.springdataredis.user;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import com.gmoon.springdataredis.cache.CachePolicy;

public interface CacheUserRepository extends CrudRepository<CacheUser, Long> {
	@Cacheable(value = CachePolicy.Name.USER, unless = "#result==null")
	Optional<CacheUser> findByUsername(String username);
}

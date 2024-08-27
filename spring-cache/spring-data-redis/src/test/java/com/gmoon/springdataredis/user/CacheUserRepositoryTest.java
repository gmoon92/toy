package com.gmoon.springdataredis.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import com.gmoon.springdataredis.cache.CachePolicy;
import com.gmoon.springdataredis.helper.CacheHelper;
import com.gmoon.springdataredis.test.ServiceTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnabledIf(value = "#{'${spring.cache.type}' == 'REDIS'}", loadContext = true)
@ServiceTest
class CacheUserRepositoryTest {

	@Autowired
	private CacheUserRepository repository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void init(@Autowired CacheHelper cacheHelper) {
		cacheHelper.evictAll();
	}

	@Test
	void save() {
		repository.save(CacheUser.builder()
			 .username("gmoon")
			 .build());

		Optional<CacheUser> actual = repository.findByUsername("gmoon");

		log.info("actual: {}", actual);
		assertThat(actual)
			 .isEqualTo(getCachedUser("gmoon"));
	}

	private Optional<CacheUser> getCachedUser(String username) {
		return Optional.ofNullable(cacheManager.getCache(CachePolicy.Name.USER))
			 .map(cache -> cache.get(username, CacheUser.class));
	}
}

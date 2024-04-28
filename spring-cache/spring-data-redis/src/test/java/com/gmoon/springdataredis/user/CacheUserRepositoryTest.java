package com.gmoon.springdataredis.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.gmoon.springdataredis.test.EmbeddedRedisConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("local")
@Import(EmbeddedRedisConfig.class)
@SpringBootTest
class CacheUserRepositoryTest {
	@Autowired CacheUserRepository repository;
	@Autowired CacheManager cacheManager;

	@BeforeEach
	void init() {
		CacheUser cacheUser = CacheUser.builder()
			.username("gmoon")
			.build();

		repository.save(cacheUser);
	}

	@Test
	void testSave() {
		// when
		Optional<CacheUser> actual = repository.findByUsername("gmoon");

		// then
		log.info("actual: {}", actual);
		assertThat(actual)
			.isNotEmpty()
			.isEqualTo(getCachedUser("gmoon"));
	}

	private Optional<CacheUser> getCachedUser(String username) {
		return Optional.ofNullable(cacheManager.getCache(CacheUser.KEY))
			.map(cache -> cache.get(username, CacheUser.class));
	}
}

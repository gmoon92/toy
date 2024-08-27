package com.gmoon.springdataredis.helper;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import com.gmoon.springdataredis.test.ServiceTest;
import com.gmoon.springdataredis.user.User;

@EnabledIf(value = "#{'${spring.cache.type}' == 'REDIS'}", loadContext = true)
@ServiceTest
class CacheHelperTest {

	@Autowired
	private CacheHelper helper;

	@BeforeEach
	void setUp() {
		helper.evictAll();
	}

	@Test
	void simple() {
		String cacheName = "simple";
		String cacheKey = "test-cache-key01";
		String cacheValue = "simple value type";

		helper.put(cacheName, cacheKey, cacheValue);
		assertThat(helper.<String>get(cacheName, cacheKey)).isEqualTo(cacheValue);

		helper.evict(cacheName);
		assertThat(helper.<String>get(cacheName, cacheKey)).isNull();
	}

	@Test
	void entity() {
		String cacheName = "entity";
		String cacheKey = "test-cache-key01";
		User cacheValue = newUser("gmoon");

		helper.put(cacheName, cacheKey, cacheValue);
		assertThat(helper.<User>get(cacheName, cacheKey))
			 .hasFieldOrPropertyWithValue("username", "gmoon");

		helper.evict(cacheName);
		assertThat(helper.<User>get(cacheName, cacheKey)).isNull();
	}

	private User newUser(String username) {
		return User.builder()
			 .username(username)
			 .build();
	}

	@Test
	void collection() {
		String cacheName = "collection";
		String cacheKey = "test-cache-key01";
		List<User> cacheValue =
			 asList(newUser("gmoon"), newUser("kim"), newUser("lee"));

		helper.put(cacheName, cacheKey, cacheValue);
		assertThat(helper.<List<User>>get(cacheName, cacheKey))
			 .map(User::getUsername)
			 .contains("gmoon", "kim", "lee");

		helper.evict(cacheName);
		assertThat(helper.<List<User>>get(cacheName, cacheKey)).isNull();
	}
}

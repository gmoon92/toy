package com.gmoon.hibernatesecondlevelcache.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;

import com.gmoon.hibernatesecondlevelcache.config.CachePolicy;
import com.gmoon.hibernatesecondlevelcache.member.Member;

@SpringBootTest
class CacheUtilTest {

	@Autowired
	private CacheUtil cacheUtil;
	private String cacheName;

	@BeforeEach
	void setUp() {
		CachePolicy policy = CachePolicy.MEMBER;
		cacheName = policy.getCacheName();
	}

	@Test
	void getCache() {
		Cache cache = cacheUtil.getCache(cacheName);

		assertThat(cache).isNotNull();
	}

	@Test
	void getValue() {
		String cacheKey = "UUID";

		cacheUtil.put(
			 cacheName,
			 cacheKey,
			 new Member()
		);

		assertThat(cacheUtil.getValue(cacheName, cacheKey))
			 .isInstanceOf(Member.class);
	}

	@Test
	void evict() {
		String cacheKey = "UUID";

		assertThatCode(() -> cacheUtil.evict(cacheName, cacheKey))
			 .doesNotThrowAnyException();
	}

	@Test
	void evictAll() {
		assertThatCode(() -> cacheUtil.evictAll(cacheName))
			 .doesNotThrowAnyException();
	}
}

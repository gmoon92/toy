package com.gmoon.springdataredis.cache;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CacheNameTest {
	@Test
	void testGetAll() {
		// when then
		assertThat(CacheName.getAll())
			.hasSize(CacheName.values().length);
	}
}

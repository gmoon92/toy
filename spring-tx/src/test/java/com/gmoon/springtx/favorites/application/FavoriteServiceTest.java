package com.gmoon.springtx.favorites.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springtx.global.Fixtures;

@SpringBootTest
class FavoriteServiceTest {

	@Autowired
	private FavoriteService service;

	@Test
	void deleteAll() {
		String userId = Fixtures.USER_ID;

		assertThatCode(() -> service.delete(userId))
			.doesNotThrowAnyException();
	}
}

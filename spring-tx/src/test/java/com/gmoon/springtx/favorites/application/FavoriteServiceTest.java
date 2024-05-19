package com.gmoon.springtx.favorites.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springtx.global.Fixtures;

@SpringBootTest
class FavoriteServiceTest {

	@Autowired
	private FavoriteService service;

	@DisplayName("강제 예외 발생")
	@Test
	void deleteAll() {
		String userId = Fixtures.USER_ID;

		assertThatThrownBy(() -> service.delete(userId))
			 .isInstanceOf(RuntimeException.class);
	}
}

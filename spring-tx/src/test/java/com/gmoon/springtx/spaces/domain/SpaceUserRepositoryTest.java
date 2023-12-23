package com.gmoon.springtx.spaces.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.springtx.global.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
class SpaceUserRepositoryTest {

	@Autowired
	private SpaceUserRepository repository;

	@Test
	void findAll() {
		repository.findAll();
	}
}

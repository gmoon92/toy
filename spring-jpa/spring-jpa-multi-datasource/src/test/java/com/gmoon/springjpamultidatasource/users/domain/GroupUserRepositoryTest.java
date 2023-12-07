package com.gmoon.springjpamultidatasource.users.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpamultidatasource.global.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
class GroupUserRepositoryTest {

	@Autowired
	private GroupUserRepository repository;

	@Test
	void findAll() {
		repository.findAll();
	}
}

package com.gmoon.springdatar2dbc.teams.team.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TeamRepositoryTest {

	@Autowired
	private TeamRepository teamRepository;

	@Test
	void findAll() {
		teamRepository.findAll();
	}
}

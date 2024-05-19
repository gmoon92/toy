package com.gmoon.springsecurityjwt.team;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TeamServiceTest {
	@Autowired
	TeamService service;

	@Test
	void testGet() {
		// given
		long teamId = 0;

		// when
		Team team = service.get(teamId);

		// then
		assertThat(team).isEqualTo(Team.create("web1"));
	}

	@Test
	void testDelete() {
		// given
		long teamId = 0L;

		// when
		service.delete(teamId);

		// then
		assertThat(service.get(teamId)).isNull();
	}
}

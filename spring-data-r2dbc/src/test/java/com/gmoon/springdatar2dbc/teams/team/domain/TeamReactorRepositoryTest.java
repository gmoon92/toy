package com.gmoon.springdatar2dbc.teams.team.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import reactor.core.publisher.Flux;

@EnabledIf(
	 // value = "'${spring.data.r2dbc.repositories.enabled}'.matches('(?i)^(1|true|yes|on)$')",
	 value = "${spring.data.r2dbc.repositories.enabled}",
	 loadContext = true
)
@SpringBootTest
class TeamReactorRepositoryTest {

	@Autowired
	private TeamReactorRepository teamReactorRepository;

	@Test
	void findAll() {
		Flux<Team> all = teamReactorRepository.findAll();
		all.blockFirst();
	}
}

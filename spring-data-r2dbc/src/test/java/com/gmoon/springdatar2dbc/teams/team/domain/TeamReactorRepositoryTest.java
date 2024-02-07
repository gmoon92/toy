package com.gmoon.springdatar2dbc.teams.team.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Flux;

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

package com.gmoon.springsecurityjwt.team;

import com.gmoon.springsecurityjwt.base.AbstractJpaRepositoryTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class TeamRepositoryTest extends AbstractJpaRepositoryTest {
	final TeamRepository repository;

	@Test
	@ExpectInsert
	void testSave() {
		// given
		Team team = Team.create("ahea");

		// when
		repository.save(team);

		// then
		flushAndClear();
	}

	@Test
	@ExpectSelect
	void testFindAll() {
		// when
		List<Team> teams = repository.findAll();

		// then
		assertThat(teams).isNotEmpty();
	}

	@Test
	@ExpectSelect
	void testGetId() {
		// given
		Long id = 0L;

		// when
		Team team = repository.getById(id);

		// then
		assertThat(team).isEqualTo(Team.create("web1"));
		assertThat(team.getUsers()).isEmpty();
	}
}

package com.gmoon.core.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.core.service.TeamService;

@SpringBootTest
class TeamConfigTest {

	@Autowired(required = false)
	private TeamService teamService;

	@DisplayName("runtime.properties 적용되는지")
	@Test
	void conditionalOnPropertyAtRuntimeProperties() {
		assertThat(teamService).isNotNull();
	}
}

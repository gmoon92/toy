package com.gmoon.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.core.service.TeamService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "team.enabled", havingValue = "true")
public class TeamConfig {

	@Bean
	public TeamService teamService() {
		return new TeamService();
	}
}

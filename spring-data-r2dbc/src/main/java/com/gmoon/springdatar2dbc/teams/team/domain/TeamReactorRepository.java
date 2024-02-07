package com.gmoon.springdatar2dbc.teams.team.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.gmoon.springdatar2dbc.global.ReactRepository;

@ReactRepository
public interface TeamReactorRepository extends ReactiveCrudRepository<Team, String> {
}

package com.gmoon.springsecurityjwt.team;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TeamService {
	private final TeamRepository repository;

	public Team get(Long teamId) {
		return repository.getById(teamId);
	}

	@Transactional
	public void delete(Long teamId) {
		Team team = get(teamId)
			 .clearUsers();

		repository.delete(team);
	}
}

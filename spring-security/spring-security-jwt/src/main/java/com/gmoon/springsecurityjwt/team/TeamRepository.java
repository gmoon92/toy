package com.gmoon.springsecurityjwt.team;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long>, TeamRepositoryCustom {
	Team getById(Long teamId);
}

package com.gmoon.quickperf.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.quickperf.domain.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
	Team findByName(String name);
}

package com.gmoon.demo.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.demo.team.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

	Team findByName(String name);
}

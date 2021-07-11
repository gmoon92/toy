package com.gmoon.demo.team.repository;

import com.gmoon.demo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

  Team findByName(String name);
}

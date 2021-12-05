package com.gmoon.quickperf.repository;

import com.gmoon.quickperf.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
  Team findByName(String name);
}

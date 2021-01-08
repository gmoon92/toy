package com.gmoon.demo.repository;

import com.gmoon.demo.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Team findByName(String name);
}

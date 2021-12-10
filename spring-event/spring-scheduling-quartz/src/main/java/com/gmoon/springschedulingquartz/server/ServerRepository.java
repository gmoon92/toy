package com.gmoon.springschedulingquartz.server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long>,
        ServerRepositoryQueryDsl {
  Server findServerByName(String name);
}

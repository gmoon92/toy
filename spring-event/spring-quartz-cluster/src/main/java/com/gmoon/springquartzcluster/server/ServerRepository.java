package com.gmoon.springquartzcluster.server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long>,
        ServerRepositoryQueryDsl {
  Server findServerByName(String name);
}

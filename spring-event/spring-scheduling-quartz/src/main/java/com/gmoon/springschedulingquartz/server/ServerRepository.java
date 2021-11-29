package com.gmoon.springschedulingquartz.server;

import org.springframework.data.repository.CrudRepository;

public interface ServerRepository extends CrudRepository<Server, Long> {
  Server findServerByName(String name);
}

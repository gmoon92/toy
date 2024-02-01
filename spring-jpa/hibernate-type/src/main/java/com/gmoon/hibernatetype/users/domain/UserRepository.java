package com.gmoon.hibernatetype.users.domain;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String>,
	UserRepositoryQuery {
}

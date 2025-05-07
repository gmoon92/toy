package com.gmoon.hibernatetype.users.infra;

import com.gmoon.hibernatetype.users.domain.User;
import org.springframework.data.repository.CrudRepository;

interface JpaUserRepository extends CrudRepository<User, String>,
	 UserRepositoryQuery {
}

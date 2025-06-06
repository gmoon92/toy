package com.gmoon.jacoco.users.domain;

import java.util.Optional;

public interface UserRepository {

	User get(String id);
	Optional<User> findByUsername(String username);

}

package com.gmoon.hibernatetype.users.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	Optional<User> findById(String id);
	User save(User user);
	List<User> findAllByEncEmail(String email);
}

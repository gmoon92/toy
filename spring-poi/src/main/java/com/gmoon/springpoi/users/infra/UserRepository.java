package com.gmoon.springpoi.users.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springpoi.users.domain.User;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByUsername(String username);
}

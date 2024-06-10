package com.gmoon.jacoco.users.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.jacoco.users.domain.User;

public interface JpaUserRepository extends JpaRepository<User, String> {

	Optional<User> findByUsername(String username);
}

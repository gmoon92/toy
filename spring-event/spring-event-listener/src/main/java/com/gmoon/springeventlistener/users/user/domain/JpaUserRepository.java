package com.gmoon.springeventlistener.users.user.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, String> {

	Optional<User> findUserByNameAndEmail(String name, String email);
}

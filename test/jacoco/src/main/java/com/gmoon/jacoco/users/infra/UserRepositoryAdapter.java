package com.gmoon.jacoco.users.infra;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gmoon.jacoco.users.domain.User;
import com.gmoon.jacoco.users.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JpaUserRepository repository;

	@Override
	public User get(String id) {
		return repository.getReferenceById(id);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return repository.findByUsername(username);
	}
}

package com.gmoon.hibernatetype.users.infra;

import com.gmoon.hibernatetype.users.domain.User;
import com.gmoon.hibernatetype.users.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JpaUserRepository repository;

	@Override
	public Optional<User> findById(String id) {
		return repository.findById(id);
	}

	@Override
	public User save(User user) {
		return repository.save(user);
	}

	@Override
	public List<User> findAllByEncEmail(String email) {
		return repository.findAllByEncEmail(email);
	}
}

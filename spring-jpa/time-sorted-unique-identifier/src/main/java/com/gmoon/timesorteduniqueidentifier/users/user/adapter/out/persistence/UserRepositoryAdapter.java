package com.gmoon.timesorteduniqueidentifier.users.user.adapter.out.persistence;

import com.gmoon.timesorteduniqueidentifier.global.base.PersistenceAdapter;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.LoadUserPort;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.UpdateUserPasswordPort;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
class UserRepositoryAdapter implements UserRepository,
	 LoadUserPort,
	 UpdateUserPasswordPort {

	private final JpaUserRepository repository;

	@Override
	public List<User> findAll() {
		return repository.findAll();
	}

	@Override
	public User get(String id) {
		return repository.getReferenceById(id);
	}

	@Override
	public User save(User user) {
		return repository.save(user);
	}
}

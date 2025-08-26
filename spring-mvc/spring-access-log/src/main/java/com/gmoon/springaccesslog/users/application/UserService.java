package com.gmoon.springaccesslog.users.application;

import com.gmoon.springaccesslog.users.domain.User;
import com.gmoon.springaccesslog.users.infra.InMemoryUserRepository;
import com.gmoon.springaccesslog.users.model.UserForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private final InMemoryUserRepository repository;

	public List<User> getAll() {
		return repository.findAll();
	}

	public User get(String id) {
		return repository.get(id);
	}

	public UserForm getView(String id) {
		var user = get(id);
		return new UserForm(user);
	}

	public User save(UserForm userForm) {
		return repository.save(new User(
			 UUID.randomUUID().toString(),
			 userForm.getUsername(),
			 "123",
			 userForm.getAge()
		));
	}

	public UserForm update(String id, UserForm userForm) {
		var user = repository.get(id);
		user.setUsername(userForm.getUsername());
		user.setAge(userForm.getAge());
		repository.save(user);
		return new UserForm(user);
	}

	public void remove(String id) {
		repository.delete(id);
	}
}

package com.rsupport.rv5x.springaccesslog.users.application;

import com.rsupport.rv5x.springaccesslog.users.domain.User;
import com.rsupport.rv5x.springaccesslog.users.infra.InMemoryUserRepository;
import com.rsupport.rv5x.springaccesslog.users.model.UserForm;
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

	public UserForm save(UserForm userForm) {
		var savedUser = repository.save(new User(
			 UUID.randomUUID().toString(),
			 userForm.getUsername(),
			 "123",
			 userForm.getAge()
		));
		return new UserForm(savedUser);
	}

	public UserForm update(UserForm userForm) {
		var user = repository.get(userForm.getId());
		user.setUsername(userForm.getUsername());
		user.setAge(userForm.getAge());
		repository.save(user);
		return new UserForm(user);
	}

	public void remove(String id) {
		repository.delete(id);
	}
}

package com.gmoon.springaccesslog.users.infra;

import com.gmoon.springaccesslog.users.domain.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryUserRepository {

	private static final Map<String, User> ALL;

	static {
		ALL = new HashMap<>();
		ALL.put("user01", new User("user01", "admin", "123", 10));
		ALL.put("user03", new User("user03", "gmoon", "123", 10));
		ALL.put("user02", new User("user02", "user", "123", 10));
	}

	public List<User> findAll() {
		return new ArrayList<>(ALL.values());
	}

	public User get(String id) {
		return Optional.ofNullable(ALL.get(id))
			 .orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	public User find(String id) {
		return ALL.get(id);
	}

	public User save(User user) {
		ALL.put(user.getId(), user);
		return user;
	}

	public void delete(String id) {
		ALL.remove(id);
	}
}

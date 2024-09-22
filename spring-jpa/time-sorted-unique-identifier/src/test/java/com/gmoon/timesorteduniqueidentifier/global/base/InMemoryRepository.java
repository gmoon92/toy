package com.gmoon.timesorteduniqueidentifier.global.base;

import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.LoadUserPort;
import com.gmoon.timesorteduniqueidentifier.users.user.application.port.out.UpdateUserPasswordPort;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.Fixtures;
import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;

import java.util.*;

public class InMemoryRepository implements LoadUserPort, UpdateUserPasswordPort {

	private static final Map<String, User> CONTEXT = Collections.synchronizedMap(new HashMap<>());

	static {
		CONTEXT.put(Fixtures.Users.ADMIN.getId(), Fixtures.Users.ADMIN);
		CONTEXT.put(Fixtures.Users.USER.getId(), Fixtures.Users.USER);
	}

	@Override
	public List<User> findAll() {
		return new ArrayList<>(CONTEXT.values());
	}

	@Override
	public User get(String id) {
		return CONTEXT.get(id);
	}

	@Override
	public User save(User user) {
		return CONTEXT.put(user.getId(), user);
	}
}

package com.gmoon.timesorteduniqueidentifier.users.user.application.port.out;

import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;

import java.util.List;

public interface LoadUserPort {

	List<User> findAll();
	User get(String id);
}

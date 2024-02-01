package com.gmoon.hibernatetype.users.domain;

import java.util.List;

public interface UserRepositoryQuery {

	List<User> findAllByEncEmail(String email);
}

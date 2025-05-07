package com.gmoon.hibernatetype.users.infra;

import com.gmoon.hibernatetype.users.domain.User;

import java.util.List;

interface UserRepositoryQuery {

	List<User> findAllByEncEmail(String email);
}

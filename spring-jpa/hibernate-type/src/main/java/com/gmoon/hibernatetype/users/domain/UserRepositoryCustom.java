package com.gmoon.hibernatetype.users.domain;

import java.util.List;

public interface UserRepositoryCustom {

	List<User> findAllByEncEmail(String email);
}

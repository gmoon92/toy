package com.gmoon.springsecurityjwt.user;

import java.util.Optional;

public interface UserRepositoryCustom {
	Optional<User> findAdminUser();
}

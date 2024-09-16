package com.gmoon.timesorteduniqueidentifier.users.user.domain;

import java.util.List;

/***
 * @apiNote Repository Pattern
 */
public interface UserRepository {

	User get(String id);

	User save(User user);

	List<User> findAll();
}

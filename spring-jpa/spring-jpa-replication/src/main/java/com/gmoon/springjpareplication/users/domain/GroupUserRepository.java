package com.gmoon.springjpareplication.users.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface GroupUserRepository extends CrudRepository<GroupUser, String> {

	List<GroupUser> findByGroupId(String groupId);
	GroupUser findByGroupIdAndUserId(String groupId, String userId);
}

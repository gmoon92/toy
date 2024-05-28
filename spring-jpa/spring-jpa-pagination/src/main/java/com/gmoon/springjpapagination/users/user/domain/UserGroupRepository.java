package com.gmoon.springjpapagination.users.user.domain;

import java.util.List;

import com.gmoon.springjpapagination.global.domain.BasePageable;

public interface UserGroupRepository {

	List<UserGroup> findAll(String groupId, String keyword, BasePageable pageable);

	long countBy(String groupId, String keyword);
}

package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUserGroup.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;

import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.global.domain.BaseRepository;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserGroupRepositoryAdapter extends BaseRepository implements UserGroupRepository {

	private final JpaUserGroupRepository repository;

	@Override
	public List<UserGroup> findAll(String groupId, String keyword, BasePageable pageable) {
		return getGroupQuery(groupId, keyword, pageable)
			 .fetch();
	}

	private JPAQuery<UserGroup> getGroupQuery(String groupId, String keyword, BasePageable pageable) {
		return pagingQuery(pageable)
			 .select(userGroup)
			 .from(userGroup)
			 .where(
				  userGroup.assignedGroup(groupId),
				  userGroup.likeName(keyword)
			 );
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return countQuery(
			 getGroupQuery(groupId, keyword, BasePageable.unpaged())
		);
	}
}

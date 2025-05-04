package com.gmoon.springjpapagination.users.user.infra;

import com.gmoon.springjpapagination.global.domain.AbstractJpaRepository;
import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gmoon.springjpapagination.users.user.domain.QUserGroup.userGroup;

@Repository
@RequiredArgsConstructor
public class UserGroupRepositoryAdapter extends AbstractJpaRepository implements UserGroupRepository {

	private final JpaUserGroupRepository repository;

	@Override
	public List<UserGroup> findAll(String groupId, String keyword, BasePageable pageable) {
		return getGroupQuery(groupId, keyword, pageable)
			 .fetch();
	}

	private JPAQuery<UserGroup> getGroupQuery(String groupId, String keyword, BasePageable pageable) {
		return fetchAndPaginated(pageable)
			 .select(userGroup)
			 .from(userGroup)
			 .where(
				  userGroup.assignedGroup(groupId),
				  userGroup.likeName(keyword)
			 );
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return fetchTotalCount(
			 getGroupQuery(groupId, keyword, BasePageable.unpaged())
		);
	}
}

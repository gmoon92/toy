package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUserGroup.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.global.domain.BaseRepository;
import com.gmoon.springjpapagination.global.domain.Pageable;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserGroupRepositoryAdapter extends BaseRepository implements UserGroupRepository {

	private final JpaUserGroupRepository repository;

	@Override
	public List<UserGroup> findAll(String groupId, String keyword, Pageable pageable) {
		return pagingQuery(pageable)
			.select(userGroup)
			.from(userGroup)
			.where(
				findAssignedGroup(groupId),
				searchKeyword(keyword)
			)
			.fetch();
	}

	private Predicate searchKeyword(String keyword) {
		if (StringUtils.isBlank(keyword)) {
			return null;
		}
		return userGroup.name.like(keyword + "%");
	}

	private Predicate findAssignedGroup(String groupId) {
		if (StringUtils.isBlank(groupId)) {
			return null;
		}
		return userGroup.id.eq(groupId);
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return countQuery(
			queryFactory
				.selectFrom(userGroup)
				.where(
					findAssignedGroup(groupId),
					searchKeyword(keyword)
				)
		);
	}
}

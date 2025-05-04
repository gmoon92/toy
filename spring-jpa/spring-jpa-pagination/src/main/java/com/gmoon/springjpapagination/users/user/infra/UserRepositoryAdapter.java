package com.gmoon.springjpapagination.users.user.infra;

import com.gmoon.springjpapagination.global.domain.AbstractJpaRepository;
import com.gmoon.springjpapagination.global.domain.BasePageable;
import com.gmoon.springjpapagination.users.user.domain.User;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gmoon.springjpapagination.users.user.domain.QUser.user;
import static com.gmoon.springjpapagination.users.user.domain.QUserGroup.userGroup;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter
	 extends AbstractJpaRepository
	 implements UserRepository {

	private final JpaUserRepository repository;

	@Override
	public Page<User> findAll(Predicate predicate, Pageable pageable) {
		return repository.findAll(predicate, pageable);
	}

	@Override
	public List<User> findAll(String groupId, String keyword, BasePageable pageable) {
		return getUserQuery(groupId, keyword, pageable)
			 .fetch();
	}

	private JPAQuery<User> getUserQuery(String groupId, String keyword, BasePageable pageable) {
		return fetchAndPaginated(pageable)
			 .select(user)
			 .from(user)
			 .join(user.userGroup, userGroup)
			 .where(
				  userGroup.assignedGroup(groupId),
				  user.likeName(keyword)
			 )
			 .orderBy(user.username.desc());
	}

	@Override
	public long countBy(String groupId, String keyword) {
		return fetchTotalCount(
			 getUserQuery(
				  groupId,
				  keyword,
				  BasePageable.unpaged()
			 )
		);
	}
}

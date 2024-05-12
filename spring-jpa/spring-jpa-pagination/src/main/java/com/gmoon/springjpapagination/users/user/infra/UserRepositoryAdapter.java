package com.gmoon.springjpapagination.users.user.infra;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.QUserContentListVO_Data;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO.Data.Type;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO.Search.KeywordType;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final JPAQueryFactory queryFactory;
	private final JpaUserRepository repository;

	@Override
	public UserContentListVO getUserContentListVO(UserContentListVO listVO) {
		UserContentListVO.Search search = listVO.getSearch();

		List<UserContentListVO.Data> dataList = queryFactory.select(new QUserContentListVO_Data(
				Expressions.asEnum(Type.USER),
				user.id,
				user.username
			))
			.from(user)
			.where(
				findAssignedGroup(search),
				searchKeyword(search)
			)
			.limit(listVO.getPageSize())
			.offset(listVO.getOffset())
			.fetch();

		listVO.setData(dataList);
		return listVO;
	}

	private Predicate findAssignedGroup(UserContentListVO.Search search) {
		String groupId = search.getGroupId();
		if (StringUtils.isNotBlank(groupId)) {
			return user.userGroup.id.eq(groupId);
		}

		return null;
	}

	private Predicate searchKeyword(UserContentListVO.Search search) {
		KeywordType keywordType = search.getKeywordType();
		String keyword = search.getKeyword();

		if (keywordType != null && StringUtils.isNotBlank(keyword)) {
			if (KeywordType.USERNAME == keywordType) {
				return user.username.like(keyword + "%");
			}
		}

		return null;
	}
}

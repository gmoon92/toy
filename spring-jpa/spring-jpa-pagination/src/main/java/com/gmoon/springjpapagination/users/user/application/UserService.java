package com.gmoon.springjpapagination.users.user.application;

import static com.gmoon.springjpapagination.users.user.domain.QUser.*;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import com.gmoon.springjpapagination.global.domain.UnionPageResizer;
import com.gmoon.springjpapagination.global.domain.UnionPagination;
import com.gmoon.springjpapagination.users.user.domain.User;
import com.gmoon.springjpapagination.users.user.domain.UserGroupRepository;
import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;
import com.gmoon.springjpapagination.users.user.dto.UserGroupListVO;
import com.gmoon.springjpapagination.users.user.dto.UserListVO;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserGroupRepository userGroupRepository;

	public UserContentListVO getUserContentListVO(UserContentListVO listVO) {
		UserContentListVO.Search search = listVO.getSearch();

		UnionPageResizer resizer = new UnionPageResizer(
			 listVO,
			 asList(
				  UnionPagination.create(
					   UserGroupListVO.class,
					   () -> userGroupRepository.countBy(search.getGroupId(), search.getKeyword())
				  ),
				  UnionPagination.create(
					   UserListVO.class,
					   () -> userRepository.countBy(search.getGroupId(), search.getKeyword())
				  )
			 )
		);

		setContent(listVO, resizer);
		return listVO;
	}

	public Page<User> findAll(String groupId, String keyword, Pageable pageable) {
		Predicate predicate = user.userGroup.id.eq(groupId)
			 .and(user.likeName(keyword));

		return userRepository.findAll(predicate, pageable);
	}

	private void setContent(UserContentListVO listVO, UnionPageResizer resizer) {
		UserContentListVO.Search search = listVO.getSearch();

		List<UserContentVO> groups = resizer.applyContent(
				  UserGroupListVO.class,
				  pageable ->
					   userGroupRepository.findAll(search.getGroupId(), search.getKeyword(), pageable)
			 )
			 .stream()
			 .flatMap(Collection::stream)
			 .map(UserContentVO::new)
			 .toList();

		List<UserContentVO> users = resizer.applyContent(
				  UserListVO.class,
				  pageable -> userRepository.findAll(search.getGroupId(), search.getKeyword(), pageable)
			 )
			 .stream()
			 .flatMap(Collection::stream)
			 .map(UserContentVO::new)
			 .toList();

		List<UserContentVO> content = new ArrayList<>(listVO.getPageSize());
		content.addAll(groups);
		content.addAll(users);

		listVO.setContent(content);
	}
}

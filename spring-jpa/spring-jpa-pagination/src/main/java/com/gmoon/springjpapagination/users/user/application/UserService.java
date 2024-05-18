package com.gmoon.springjpapagination.users.user.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository repository;

	@Transactional(readOnly = true)
	public UserContentListVO getUserContentListVO(UserContentListVO listVO) {
		UserContentListVO.Search search = listVO.getSearch();

		long count = repository.countBy(search.getGroupId(), search.getKeyword());
		listVO.setTotalCount(count);

		List<UserContentVO> userContents = repository.findAll(
				search.getGroupId(),
				search.getKeyword(),
				listVO
			)
			.stream()
			.map(UserContentVO::new)
			.collect(Collectors.toList());

		listVO.setContent(userContents);
		return listVO;
	}
}

package com.gmoon.springjpapagination.users.user.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository repository;

	public UserContentListVO getUserContentListVO(UserContentListVO listVO) {
		UserContentListVO.Search search = listVO.getSearch();

		long count = repository.getUserContentTotalCount(search.getGroupId(), search.getKeyword());
		listVO.setTotalCount(count);

		List<UserVO> userContents = repository.getUserContents(
			search.getGroupId(),
			search.getKeyword(),
			listVO
		);

		listVO.setContent(userContents);
		return listVO;
	}
}

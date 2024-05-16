package com.gmoon.springjpapagination.users.user.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gmoon.springjpapagination.users.user.domain.UserRepository;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository repository;

	public UserContentListVO getUserContentListVO(UserContentListVO listVO) {
		UserContentListVO.Search search = listVO.getSearch();

		long count = repository.getUserContentTotalCount(search.getGroupId(), search.getKeyword());
		listVO.setTotalCount(count);

		List<UserContentVO> userContents = repository.getUserContents(
			search.getGroupId(),
			search.getKeyword(),
			listVO
		);

		listVO.setData(userContents);
		return listVO;
	}
}

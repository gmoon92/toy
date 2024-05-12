package com.gmoon.springjpapagination.users.user.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.gmoon.springjpapagination.users.user.dto.UserContentVO;

public interface UserRepository {

	List<UserContentVO> getUserContents(String groupId, String keyword, Pageable pageable);

	long getUserContentTotalCount(String groupId, String keyword);
}

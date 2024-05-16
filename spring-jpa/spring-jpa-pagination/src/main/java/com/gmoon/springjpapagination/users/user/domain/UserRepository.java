package com.gmoon.springjpapagination.users.user.domain;

import java.util.List;

import com.gmoon.springjpapagination.global.domain.Pageable;
import com.gmoon.springjpapagination.users.user.dto.UserVO;

public interface UserRepository {

	List<UserVO> getUserContents(String groupId, String keyword, Pageable pageable);

	long getUserContentTotalCount(String groupId, String keyword);
}

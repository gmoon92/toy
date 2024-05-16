package com.gmoon.springjpapagination.users.user.domain;

import java.util.List;

import com.gmoon.springjpapagination.global.domain.BasePaginatedVO;
import com.gmoon.springjpapagination.users.user.dto.UserContentVO;

public interface UserRepository {

	List<UserContentVO> getUserContents(String groupId, String keyword, BasePaginatedVO pageable);

	long getUserContentTotalCount(String groupId, String keyword);
}

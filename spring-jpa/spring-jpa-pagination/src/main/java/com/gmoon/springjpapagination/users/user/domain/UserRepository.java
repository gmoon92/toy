package com.gmoon.springjpapagination.users.user.domain;

import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;

public interface UserRepository {

	UserContentListVO getUserContentListVO(UserContentListVO listVO);
}

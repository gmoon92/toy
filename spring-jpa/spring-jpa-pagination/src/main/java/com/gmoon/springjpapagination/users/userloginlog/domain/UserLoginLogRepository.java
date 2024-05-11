package com.gmoon.springjpapagination.users.userloginlog.domain;

import java.util.List;

import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

public interface UserLoginLogRepository {

	UserLoginLog save(UserLoginLog userLoginLog);

	List<UserLoginLog> findAll();

	UserLoginLog get(String id);

	UserLoginLogListVO getUserLoginLogListVO(UserLoginLogListVO listVO);

}

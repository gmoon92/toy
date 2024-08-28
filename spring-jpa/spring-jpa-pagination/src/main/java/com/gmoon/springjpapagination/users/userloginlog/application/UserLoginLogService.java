package com.gmoon.springjpapagination.users.userloginlog.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLogRepository;
import com.gmoon.springjpapagination.users.userloginlog.dto.UserLoginLogListVO;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserLoginLogService {

	private final UserLoginLogRepository repository;

	public UserLoginLogListVO getUserLoginLogListVO(UserLoginLogListVO listVO) {
		return repository.getUserLoginLogListVO(listVO);
	}
}

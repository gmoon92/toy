package com.gmoon.springjpapagination.users.userloginlog.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springjpapagination.users.userloginlog.domain.UserLoginLog;

public interface JpaUserLoginLogRepository extends JpaRepository<UserLoginLog, String> {
}

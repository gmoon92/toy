package com.gmoon.springjpapagination.users.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springjpapagination.users.user.domain.UserGroup;

public interface JpaUserGroupRepository extends JpaRepository<UserGroup, String> {
}

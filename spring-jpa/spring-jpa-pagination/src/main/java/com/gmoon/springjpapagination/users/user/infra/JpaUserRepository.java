package com.gmoon.springjpapagination.users.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.springjpapagination.users.user.domain.User;

public interface JpaUserRepository extends JpaRepository<User, String> {
}

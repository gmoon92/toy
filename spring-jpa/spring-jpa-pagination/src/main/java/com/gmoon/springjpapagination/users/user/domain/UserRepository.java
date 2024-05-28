package com.gmoon.springjpapagination.users.user.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import com.gmoon.springjpapagination.global.domain.BasePageable;

public interface UserRepository {

	List<User> findAll(String groupId, String keyword, BasePageable pageable);

	Page<User> findAll(Predicate predicate, Pageable pageable);

	long countBy(String groupId, String keyword);
}

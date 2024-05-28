package com.gmoon.springjpapagination.users.user.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springjpapagination.users.user.application.UserService;
import com.gmoon.springjpapagination.users.user.domain.User;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserRestController {

	private final UserService userService;

	@GetMapping("/content")
	public ResponseEntity<UserContentListVO> content(UserContentListVO listVO) {
		return ResponseEntity.ok(userService.getUserContentListVO(listVO));
	}

	@GetMapping
	public HttpEntity<PagedModel<User>> findAll(String groupId, String keyword, Pageable pageable) {
		Page<User> content = userService.findAll(groupId, keyword, pageable);
		return ResponseEntity.ok(new PagedModel<>(content));
	}
}

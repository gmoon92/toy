package com.gmoon.springjpapagination.users.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springjpapagination.users.user.application.UserService;
import com.gmoon.springjpapagination.users.user.dto.UserContentListVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserRestController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserContentListVO> findAll(UserContentListVO listVO) {
		return ResponseEntity.ok(userService.getUserContentListVO(listVO));
	}
}

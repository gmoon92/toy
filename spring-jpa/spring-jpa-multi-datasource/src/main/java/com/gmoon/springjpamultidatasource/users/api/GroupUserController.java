package com.gmoon.springjpamultidatasource.users.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springjpamultidatasource.users.application.GroupUserService;
import com.gmoon.springjpamultidatasource.users.domain.GroupUser;

import lombok.RequiredArgsConstructor;

@RequestMapping("/group/user")
@RestController
@RequiredArgsConstructor
public class GroupUserController {

	private final GroupUserService groupUserService;

	@ResponseBody
	@GetMapping
	public ResponseEntity<List<GroupUser>> find(String groupId) {
		return ResponseEntity.ok()
			.body(groupUserService.getGroupUsers(groupId));
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(String groupId, String userId) {
		groupUserService.delete(groupId, userId);
		return ResponseEntity.ok()
			.build();
	}
}

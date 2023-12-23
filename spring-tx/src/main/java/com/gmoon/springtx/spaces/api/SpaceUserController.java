package com.gmoon.springtx.spaces.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springtx.spaces.application.SpaceUserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/space/user")
@RestController
@RequiredArgsConstructor
public class SpaceUserController {

	private final SpaceUserService groupUserManager;

	@DeleteMapping
	public ResponseEntity<Void> delete(String spaceId, String userId) {
		groupUserManager.delete(spaceId, userId);
		return ResponseEntity.ok().build();
	}
}

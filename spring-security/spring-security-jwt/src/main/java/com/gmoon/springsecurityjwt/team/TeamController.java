package com.gmoon.springsecurityjwt.team;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
	private final TeamService service;

	@GetMapping("/{teamId}")
	@ResponseBody
	public Team get(@PathVariable Long teamId) {
		return service.get(teamId);
	}

	@DeleteMapping("/{teamId}")
	public ResponseEntity<Long> delete(@PathVariable Long teamId) {
		service.delete(teamId);
		return ResponseEntity.ok(teamId);
	}
}

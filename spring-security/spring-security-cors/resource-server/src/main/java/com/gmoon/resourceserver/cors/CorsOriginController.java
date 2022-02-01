package com.gmoon.resourceserver.cors;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@ResponseBody
@RequestMapping("/cors")
@RequiredArgsConstructor
public class CorsOriginController {
	private final CorsOriginService service;

	@GetMapping
	public ResponseEntity<List<CorsOrigin>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@PostMapping
	public ResponseEntity<CorsOrigin> save(@RequestBody Origin origin) {
		CorsOrigin corsOrigin = service.save(CorsOrigin.create(origin));
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(corsOrigin);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remove(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.build();
	}
}

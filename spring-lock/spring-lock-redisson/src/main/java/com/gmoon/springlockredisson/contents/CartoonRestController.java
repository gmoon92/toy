package com.gmoon.springlockredisson.contents;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springlockredisson.global.redisson.Lock;
import com.gmoon.springlockredisson.global.redisson.LockKeyTimePolicy;
import com.gmoon.springlockredisson.global.redisson.LockUtils;

import lombok.RequiredArgsConstructor;

@RequestMapping("/contents")
@RestController
@RequiredArgsConstructor
public class CartoonRestController {

	private final LockUtils lockUtils;
	private final CartoonService service;

	@PostMapping("/hit")
	public ResponseEntity<Long> hit() {
		Lock lock = lockUtils.createLock("content-hit", LockKeyTimePolicy.DEFAULT);
		Cartoon cartoon = lockUtils.synchronize(lock, service::hit);
		Long count = cartoon.getCount();
		return ResponseEntity.ok(count);
	}
}

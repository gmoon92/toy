package com.gmoon.springlockredisson.contents;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.springlockredisson.global.redisson.Lock;
import com.gmoon.springlockredisson.global.redisson.LockCreator;
import com.gmoon.springlockredisson.global.redisson.LockKeyTimePolicy;

import lombok.RequiredArgsConstructor;

@RequestMapping("/contents")
@RestController
@RequiredArgsConstructor
public class CartoonRestController {

	private final LockCreator lockCreator;
	private final CartoonService service;

	@PostMapping("/hit")
	public ResponseEntity<Long> hit() {
		Lock lock = lockCreator.createLock("content-hit", LockKeyTimePolicy.DEFAULT);
		Cartoon cartoon = lock.synchronize(service::hit);
		Long count = cartoon.getCount();
		return ResponseEntity.ok(count);
	}
}

package com.gmoon.springlockredisson.contents;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartoonService {

	private final CartoonRepository repository;

	public Cartoon hit() {
		Cartoon cartoon = repository.getById(0L);
		cartoon.hit();
		return repository.save(cartoon);
	}
}

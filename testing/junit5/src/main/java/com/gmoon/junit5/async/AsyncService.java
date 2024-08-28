package com.gmoon.junit5.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncService {

	@Async
	public Future<Integer> handle() {
		log.info("print...");
		return CompletableFuture.completedFuture(1);
	}
}

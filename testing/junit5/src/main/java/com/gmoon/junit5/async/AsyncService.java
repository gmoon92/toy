package com.gmoon.junit5.async;

import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncService {

	@Async
	public Future<Integer> handle() {
		log.info("print...");
		return new AsyncResult<>(1);
	}
}

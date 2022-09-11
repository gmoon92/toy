package com.gmoon.awaitility.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.awaitility.concurrency.Worker;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EventServiceTest {

	@Autowired
	EventService eventService;

	@DisplayName("비동기 검증, CountDownLatch 활용")
	@Nested
	class CountDownLatchTest {

		CountDownLatch counter;
		List<Thread> workers;

		@BeforeEach
		void setUp() {
			int threadCount = 5;
			counter = new CountDownLatch(threadCount);

			// create worker
			workers = Stream
				.generate(() -> new Worker(counter, eventService::handle))
				.map(Thread::new)
				.limit(threadCount)
				.collect(Collectors.toList());
		}

		@DisplayName("Concurrency test")
		@Test
		void case1() throws InterruptedException {
			// submit tasks
			ExecutorService executor = Executors.newCachedThreadPool();
			for (Thread worker : workers) {
				executor.submit(worker);
			}

			// wait until task is finished
			// when
			counter.await(2, TimeUnit.SECONDS);

			// then
			assertThat(counter.getCount()).isZero();
//			Thread.sleep(1000);
		}

		@DisplayName("CountDownLatch await timeout")
		@Test
		void case2() throws InterruptedException {
			// submit tasks
			ExecutorService executor = Executors.newCachedThreadPool();
			for (Thread worker : workers) {
				executor.submit(worker);
			}

			// wait until task is finished
			// when
			counter.await(1, TimeUnit.SECONDS);

			// then
			assertThat(counter.getCount()).isZero();
		}
	}
}

package com.gmoon.awaitility.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.gmoon.awaitility.concurrency.Worker;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.CoreMatchers;
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

	@Autowired
	EventRepository eventRepository;

	@DisplayName("비동기 검증, Awaitility 활용")
	@Nested
	class AwaitilityTest {

		@BeforeEach
		void setUp() {
			eventRepository.deleteAll();

			Event saveEvent = new Event("hello gmoon.");
			// 저장 시간 2초
			eventService.save(saveEvent);
		}

		@DisplayName("lambda 표현식")
		@Test
		void case1() {
			// then
			Awaitility.await("lambda 표현식")
				.pollDelay(Duration.ofSeconds(1)) // 1초 대기
				.pollInterval(Duration.ofSeconds(1)) // 1초 마다 확인
				.atMost(Duration.ofSeconds(5)) // 최대 대기 시간 설정, default 10 초
				// assertion
				// .until(() -> eventRepository.size() == 1)
				.until(eventRepository::size, CoreMatchers.equalTo(1));
		}

		@DisplayName("AssertJ 단언문 사용")
		@Test
		void withAssertJ() {
			// then
			Awaitility.await("AssertJ 단언문 사용")
				.pollDelay(Duration.ofSeconds(1)) // 1초 대기
				.pollInterval(Duration.ofSeconds(1)) // 1초 마다 확인
				.atMost(Duration.ofSeconds(5)) // 최대 대기 시간 설정, default 10 초
				.untilAsserted(
					() -> assertThat(eventRepository.size()).isNotZero()
				);
		}

		@DisplayName("Timeout 예외")
		@Test
		void error1() {
			// then
			assertThatThrownBy(() ->
					Awaitility.await("Timeout 예외")
						.atMost(Duration.ofMillis(200)) // 최대 대기 시간 설정, default 10 초
//					.timeout(Duration.ofMillis(200))
						.untilAsserted(() ->
							Assertions.assertThat(eventRepository.size())
								.isNotZero()
						)
			).isInstanceOf(ConditionTimeoutException.class);
		}
	}

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

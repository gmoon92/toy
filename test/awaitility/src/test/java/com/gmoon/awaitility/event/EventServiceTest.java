package com.gmoon.awaitility.event;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.awaitility.concurrency.Worker;

/**
 * @see <a href="https://github.com/awaitility/awaitility/wiki/Usage">Awaitility wiki</a>
 * */
class EventServiceTest {

	EventService eventService;

	@DisplayName("비동기 검증, Awaitility 활용")
	@Nested
	class AwaitilityTest {

		@BeforeEach
		void setUp() {
			EventRepository repository = new EventRepository();
			eventService = new EventService(repository);
			eventService.deleteAll();

			// 500 millisecond delay
			ExecutorService executor = Executors.newCachedThreadPool();
			executor.submit(() -> eventService.save(new Event("hello gmoon.")));
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
				 .until(eventService::count, CoreMatchers.equalTo(1));
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
					  () -> assertThat(eventService.count()).isNotZero()
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
						   Assertions.assertThat(eventService.count())
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
		int threadCount = 5;

		@BeforeEach
		void setUp() {
			EventRepository repository = new EventRepository();
			eventService = new EventService(repository);
			eventService.deleteAll();

			counter = new CountDownLatch(threadCount);

			// create worker
			workers = IntStream.range(0, threadCount)
				 .mapToObj(i ->
					  new Worker(
						   counter,
						   () -> eventService.save(new Event("gmoon" + i))
					  )
				 )
				 .map(Thread::new)
				 .toList();
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
			assertThat(eventService.count()).isEqualTo(threadCount);
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
			counter.await(1, TimeUnit.MILLISECONDS);

			// then
			assertThat(eventService.count()).isLessThan(threadCount);
		}
	}
}

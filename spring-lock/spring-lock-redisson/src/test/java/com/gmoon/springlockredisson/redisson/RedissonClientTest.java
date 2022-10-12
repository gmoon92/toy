package com.gmoon.springlockredisson.redisson;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class RedissonClientTest {

	@Autowired
	private RedissonClient redisson;

	private Executor task;

	@BeforeEach
	void setUp() {
		task = Executors.newFixedThreadPool(3);
	}

	@Test
	@Timeout(1)
	void remainTimeToLive() {
		String lockKey = "gmoon-" + UUID.randomUUID().toString();
		RLock lock = redisson.getLock(lockKey);

		lock.lock(500, TimeUnit.MICROSECONDS);
		while (lock.remainTimeToLive() > 0) {
			boolean isLocked = lock.isLocked();
			assertThat(isLocked).isTrue();
			log.info("isLocked: {}, ttl: {}", isLocked, lock.remainTimeToLive());
		}

		assertThat(lock.isLocked()).isFalse();
	}

	@DisplayName("락 해제(unlock)는 같은 스레드일 경우에만 할 수 있다.")
	@Test
	void error() {
		String lockKey = "gmoon-" + UUID.randomUUID().toString();
		RLock lock = redisson.getLock(lockKey);

		lock.lock(10, TimeUnit.SECONDS);

		task.execute(() -> assertThrows(IllegalMonitorStateException.class,
			() -> lock.unlock()));
	}

	@DisplayName("락 획득")
	@Nested
	class TryLockTest {

		@Test
		void tryLock() {
			// 락 생성
			String lockKey = "gmoon-" + UUID.randomUUID().toString();
			RLock lock = redisson.getLock(lockKey);

			try {
				int waitTime = 1; // 대기 시간
				int leaseTime = 10; // 임대 시간

				// 락 획득 시도
				if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
					log.info("save start...");
					slowSave(Duration.ofSeconds(1));
					log.info("save end...");
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				// 락 해제
				releaseLock(lock);
			}
		}

		@DisplayName("대기 시간(waitTime) 초과인 경우,"
			+ "tryLock 은 false 를 반환한다.")
		@Test
		void waitTime() {
			String lockKey = "gmoon-" + UUID.randomUUID().toString();

			task.execute(() -> {
				RLock lock = redisson.getLock(lockKey);
				lock.lock(15, TimeUnit.SECONDS);
			});

			Awaitility.await()
				.pollDelay(Duration.ofSeconds(1))
				.atMost(Duration.ofSeconds(2))
				.untilAsserted(() -> {
					RLock lock = redisson.getLock(lockKey);
					boolean hasLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
					log.info("lock.isLocked(): {}, hasLock: {}", lock.isLocked(), hasLock);
					assertThat(hasLock).isFalse();
				});
		}

		@DisplayName("지정한 임대 시간(leaseTime)이 지나면 자동으로 해제 된다."
			+ "직접 락 해제(unlock)를 하지 않는 경우 "
			+ "락을 획득한 이후 락을 유지하는 최대 시간이다."
			+ "-1 인 경우 락 해제(unlock) 될 때까지 락을 유지한다.")
		@Test
		void leaseTime() {
			String lockKey = "gmoon-" + UUID.randomUUID().toString();

			task.execute(() -> {
				RLock lock = redisson.getLock(lockKey);
				lock.lock(2, TimeUnit.SECONDS);
				lock.unlock();
			});

			Awaitility.await()
				.pollDelay(Duration.ofSeconds(1))
				.atMost(Duration.ofSeconds(2))
				.untilAsserted(() -> {
					RLock lock = redisson.getLock(lockKey);
					boolean hasLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
					log.info("lock.isLocked(): {}, hasLock: {}", lock.isLocked(), hasLock);
					assertThat(hasLock).isTrue();
				});
		}

		private void releaseLock(RLock lock) {
			if (lock.isLocked()) {
				lock.unlock();
			}
		}

		private void slowSave(Duration duration) {
			try {
				Thread.sleep(duration.toMillis());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@DisplayName("RAtomicLong 테스트")
	@Nested
	class RAtomicLongTest {

		@RepeatedTest(5)
		void incrementAndGet() {
			String lockKey = "gmoon-" + UUID.randomUUID().toString();

			RLock lock = redisson.getLock(lockKey);
			String hitCountKey = lockKey + "-hitCount";
			RAtomicLong atomicLong = redisson.getAtomicLong(hitCountKey);

			task.execute(() -> getIncrementAndGet(lock, hitCountKey));
			task.execute(() -> getIncrementAndGet(lock, hitCountKey));
			task.execute(() -> getIncrementAndGet(lock, hitCountKey));

			Awaitility.await()
				.pollDelay(Duration.ofMillis(10))
				.atMost(Duration.ofMillis(200))
				.untilAsserted(() -> assertThat(atomicLong.get()).isEqualTo(3));
		}

		private long getIncrementAndGet(RLock lock, String hitCountKey) {
			try {
				if (lock.tryLock(10, 10, TimeUnit.SECONDS)) {
					RAtomicLong atomicLong = redisson.getAtomicLong(hitCountKey);
					return atomicLong.incrementAndGet();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (lock.isLocked()) {
					lock.unlock();
				}
			}

			throw new RuntimeException("lock try timeout...");
		}
	}
}

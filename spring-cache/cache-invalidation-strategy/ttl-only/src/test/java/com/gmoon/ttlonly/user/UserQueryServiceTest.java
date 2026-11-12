package com.gmoon.ttlonly.user;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheWriter;

import com.gmoon.cacheinvalidation.test.measure.DatabaseQueryCounter;
import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.ttlonly.config.CachePolicy;

@IntegrationTest
@DisplayName("사용자 조회가 캐시를 쓰는 방식")
class UserQueryServiceTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";
	private static final Duration SCHEDULING_SLACK = Duration.ofSeconds(2);

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired CacheManager cacheManager;
	@Autowired DatabaseQueryCounter databaseQueryCounter;

	private Long userId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(CachePolicy.Name.USER).clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
		databaseQueryCounter.reset();
	}

	@Nested
	@DisplayName("같은 값을 반복 조회하면")
	class WhenReadRepeatedly {

		@Test
		@DisplayName("첫 조회만 DB를 읽고 이후는 캐시가 응답한다")
		void readsDatabaseOnlyOnce() {
			userQueryService.findById(userId);
			long afterFirstRead = databaseQueryCounter.executedStatementCount();

			userQueryService.findById(userId);
			userQueryService.findById(userId);

			assertThat(databaseQueryCounter.executedStatementCount())
				 .as("캐시 적중이면 DB 실행 문장이 늘지 않아야 한다")
				 .isEqualTo(afterFirstRead);
		}
	}

	@Nested
	@DisplayName("DB를 변경해도")
	class WhenDatabaseChanged {

		@Test
		@DisplayName("TTL 만료 전에는 옛 값을 반환한다")
		void returnsStaleValueBeforeExpiry() {
			userQueryService.findById(userId);

			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			assertThat(userQueryService.findById(userId).email())
				 .as("TTL Only 전략은 변경을 캐시에 전파하지 않는다")
				 .isEqualTo(ORIGINAL_EMAIL);
		}

		@Test
		@DisplayName("TTL이 만료되면 새 값을 반환한다")
		void returnsFreshValueAfterExpiry() {
			userQueryService.findById(userId);
			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			Awaitility.await()
				 .atMost(awaitTimeout())
				 .pollInterval(Duration.ofMillis(200))
				 .untilAsserted(() -> assertThat(userQueryService.findById(userId).email())
					  .isEqualTo(CHANGED_EMAIL));
		}

		@Test
		@DisplayName("stale window의 상한이 TTL과 일치한다")
		void staleWindowIsBoundedByTtl() {
			userQueryService.findById(userId);
			userCommandService.changeEmail(userId, CHANGED_EMAIL);
			long startedAt = System.nanoTime();
			AtomicLong lastStaleReadAt = new AtomicLong(startedAt);

			Awaitility.await()
				 .atMost(awaitTimeout())
				 .pollInterval(Duration.ofMillis(100))
				 .untilAsserted(() -> assertThat(emailRecordingStaleRead(lastStaleReadAt))
					  .as("TTL 안에 새 값이 보여야 한다")
					  .isEqualTo(CHANGED_EMAIL));

			Duration staleObservedFor = Duration.ofNanos(lastStaleReadAt.get() - startedAt);
			assertSoftly(softly -> {
				softly.assertThat(staleObservedFor)
					 .as("옛 값을 한 번도 보지 못하면 이 명제는 저절로 성립한다")
					 .isPositive();
				softly.assertThat(staleObservedFor)
					 .as("옛 값이 마지막으로 보인 시점이 설정된 만료 상한을 넘지 않아야 한다")
					 .isLessThanOrEqualTo(expiryUpperBound());
			});
		}
	}

	@Nested
	@DisplayName("존재하지 않는 사용자를 조회하면")
	class WhenUserNotFound {

		@Test
		@DisplayName("not-found도 캐시되어 DB를 반복 조회하지 않는다")
		void cachesNotFoundToPreventPenetration() {
			long unknownId = -1L;
			assertThat(userQueryService.findById(unknownId))
				 .as("존재하지 않는 사용자는 null 로 응답한다")
				 .isNull();
			long afterFirstRead = databaseQueryCounter.executedStatementCount();

			userQueryService.findById(unknownId);
			userQueryService.findById(unknownId);

			assertThat(databaseQueryCounter.executedStatementCount())
				 .as("null을 캐시하지 않으면 존재하지 않는 키가 매번 DB를 때린다 (Cache Penetration)")
				 .isEqualTo(afterFirstRead);
		}
	}

	@Nested
	@DisplayName("캐시에 저장된 값은")
	class StoredValue {

		@Test
		@DisplayName("타입 힌트 없이 저장된다")
		void hasNoTypeHint() {
			userQueryService.findById(userId);

			assertThat(cacheManager.getCache(CachePolicy.Name.USER).get(userId, UserView.class))
				 .as("캐시별 타입 직렬화기를 쓰면 @class 없이도 복원된다")
				 .isNotNull()
				 .extracting(UserView::email)
				 .isEqualTo(ORIGINAL_EMAIL);
		}
	}

	/**
	 * 캐시 설정에 실제로 등록된 TTL.
	 * <p>
	 * 정책의 TTL 을 그대로 쓰면, 설정이 정책을 반영하지 않아도 테스트가 알아채지 못한다.
	 */
	private Duration expiryUpperBound() {
		RedisCache userCache = (RedisCache)cacheManager.getCache(CachePolicy.Name.USER);
		RedisCacheWriter.TtlFunction expiry = userCache.getCacheConfiguration().getTtlFunction();
		return expiry.getTimeToLive(String.valueOf(userId), ORIGINAL_EMAIL);
	}

	/**
	 * 조회를 시작한 시각을 재고, 옛 값이 돌아왔다면 그 시각을 남긴다.
	 * <p>
	 * 새 값을 처음 본 시각으로 재면 폴링 간격과 그 조회 시간이 섞여 상한을 넘을 수 있다.
	 * 옛 값이 보였다는 것은 그 순간 키가 아직 살아 있었다는 뜻이므로 언제나 만료 전이다.
	 */
	private String emailRecordingStaleRead(AtomicLong lastStaleReadAt) {
		long readAt = System.nanoTime();
		String email = userQueryService.findById(userId).email();
		if (ORIGINAL_EMAIL.equals(email)) {
			lastStaleReadAt.set(readAt);
		}
		return email;
	}

	/**
	 * 폴링 간격과 왕복만큼의 여유. 만료 자체의 상한이 아니다.
	 */
	private Duration awaitTimeout() {
		return expiryUpperBound().plus(SCHEDULING_SLACK);
	}
}

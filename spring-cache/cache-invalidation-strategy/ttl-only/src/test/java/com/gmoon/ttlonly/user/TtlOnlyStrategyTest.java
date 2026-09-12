package com.gmoon.ttlonly.user;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import com.gmoon.cacheinvalidation.test.measure.DatabaseQueryCounter;
import com.gmoon.cacheinvalidation.test.IntegrationTest;

@IntegrationTest
@DisplayName("TTL Only 전략")
class TtlOnlyStrategyTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired CacheManager cacheManager;
	@Autowired DatabaseQueryCounter databaseQueryCounter;

	private Long userId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(UserCachePolicy.Name.USER).clear();
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
				 .atMost(ttlUpperBound())
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

			Awaitility.await()
				 .atMost(ttlUpperBound())
				 .pollInterval(Duration.ofMillis(100))
				 .untilAsserted(() -> assertThat(userQueryService.findById(userId).email())
					  .isEqualTo(CHANGED_EMAIL));

			assertThat(Duration.ofNanos(System.nanoTime() - startedAt))
				 .as("관측된 stale 지속시간이 TTL을 넘지 않아야 한다")
				 .isLessThanOrEqualTo(ttlUpperBound());
		}
	}

	@Nested
	@DisplayName("존재하지 않는 사용자를 조회하면")
	class WhenUserNotFound {

		@Test
		@DisplayName("not-found도 캐시되어 DB를 반복 조회하지 않는다")
		void cachesNotFoundToPreventPenetration() {
			long unknownId = -1L;
			assertThat(userQueryService.findById(unknownId)).isNull();
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

			assertThat(cacheManager.getCache(UserCachePolicy.Name.USER).get(userId, CachedUser.class))
				 .as("캐시별 타입 직렬화기를 쓰면 @class 없이도 복원된다")
				 .isNotNull()
				 .extracting(CachedUser::email)
				 .isEqualTo(ORIGINAL_EMAIL);
		}
	}

	private Duration ttlUpperBound() {
		Duration ttl = UserCachePolicy.USER.ttl();
		return ttl.plus(ttl.dividedBy(2));
	}
}

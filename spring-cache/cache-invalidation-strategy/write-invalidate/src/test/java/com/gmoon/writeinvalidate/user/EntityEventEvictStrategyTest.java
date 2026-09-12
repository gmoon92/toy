package com.gmoon.writeinvalidate.user;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.cacheinvalidation.test.CacheIntegrationTest;
import com.gmoon.cacheinvalidation.test.SeparateTransaction;

@CacheIntegrationTest
@DisplayName("엔티티 이벤트 무효화 - CacheEvictable 을 구현한 엔티티")
class EntityEventEvictStrategyTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired CacheManager cacheManager;
	@Autowired TransactionTemplate transactionTemplate;

	private Long userId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(UserCachePolicy.Name.USER).clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
	}

	@Nested
	@DisplayName("쓰기 경로에 캐시 코드가 없어도")
	class WhenWritePathHasNoCacheCode {

		@Test
		@DisplayName("변경 후 첫 조회가 새 값을 반환한다")
		void returnsFreshValueAfterChange() {
			userQueryService.findById(userId);

			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			assertThat(userQueryService.findById(userId).email())
				 .as("무효화가 서비스 메서드가 아니라 엔티티 변경에서 파생된다")
				 .isEqualTo(CHANGED_EMAIL);
		}

		@Test
		@DisplayName("삭제된 엔티티의 캐시도 제거된다")
		void evictsCacheOfDeletedEntity() {
			userQueryService.findById(userId);

			userCommandService.unregister(userId);

			assertThat(cachedEmail())
				 .as("POST_COMMIT_DELETE 도 같은 리스너가 처리한다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션에 참여하면")
	class WhenJoiningOuterTransaction {

		@Test
		@DisplayName("커밋 전에는 캐시를 건드리지 않는다")
		void keepsCacheUntilOuterCommit() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);

				assertThat(cachedEmail())
					 .as("아직 커밋되지 않은 변경은 무효화 근거가 될 수 없다")
					 .isEqualTo(ORIGINAL_EMAIL);
			});
		}

		@Test
		@DisplayName("커밋 전에 옛 값이 다시 적재되어도 stale 이 고착되지 않는다")
		void evictsValueRepopulatedBeforeCommit() {
			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				SeparateTransaction.read(() -> userQueryService.findById(userId));

				assertThat(cachedEmail())
					 .as("커밋 전 조회는 MISS 후 옛 값을 캐시에 적재한다")
					 .isEqualTo(ORIGINAL_EMAIL);
			});

			assertThat(userQueryService.findById(userId).email())
				 .as("무효화가 커밋 이후이므로 커밋 전에 적재된 값까지 함께 지워진다")
				 .isEqualTo(CHANGED_EMAIL);
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션이 롤백되면")
	class WhenOuterTransactionRolledBack {

		@Test
		@DisplayName("무효화가 일어나지 않는다")
		void doesNotEvictOnRollback() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				status.setRollbackOnly();
			});

			assertThat(cachedEmail())
				 .as("커밋되지 않은 변경은 POST_COMMIT 이벤트를 만들지 않는다")
				 .isEqualTo(ORIGINAL_EMAIL);
		}
	}

	private String cachedEmail() {
		CachedUser cached = cacheManager.getCache(UserCachePolicy.Name.USER)
			 .get(userId, CachedUser.class);
		return cached == null ? null : cached.email();
	}
}

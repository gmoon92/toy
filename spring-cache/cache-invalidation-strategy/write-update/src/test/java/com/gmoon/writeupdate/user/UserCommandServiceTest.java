package com.gmoon.writeupdate.user;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.cacheinvalidation.test.SeparateTransaction;
import com.gmoon.cacheinvalidation.test.measure.DatabaseQueryCounter;
import com.gmoon.writeupdate.config.CachePolicy;

@IntegrationTest
@DisplayName("캐시를 덮어쓰는 사용자 쓰기")
class UserCommandServiceTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired UserRepository userRepository;
	@Autowired DatabaseQueryCounter databaseQueryCounter;
	@Autowired TransactionTemplate transactionTemplate;
	@Autowired CacheManager cacheManager;

	private Long userId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(CachePolicy.Name.USER).clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
	}

	@Nested
	@DisplayName("쓰기가 트랜잭션을 직접 소유하면")
	class WhenWriteOwnsTransaction {

		@Test
		@DisplayName("변경 직후 조회가 새 값을 반환한다")
		void returnsNewValueRightAfterChange() {
			userQueryService.findById(userId);

			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			assertThat(userQueryService.findById(userId).email())
				 .as("옛 값을 지우는 대신 새 값으로 덮어쓴다")
				 .isEqualTo(CHANGED_EMAIL);
		}

		@Test
		@DisplayName("변경 직후 조회가 DB 를 읽지 않는다")
		void servesChangedValueWithoutDatabaseRead() {
			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			databaseQueryCounter.reset();
			userQueryService.findById(userId);

			assertThat(databaseQueryCounter.executedStatementCount())
				 .as("삭제 방식이면 MISS 후 DB 를 한 번 읽는다. 갱신 방식은 쓰기가 캐시를 채워 두므로 그 한 번이 없다")
				 .isZero();
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션에 참여하면")
	class WhenJoiningOuterTransaction {

		@Test
		@DisplayName("커밋 전에 다른 트랜잭션이 캐시로 미커밋 값을 읽는다")
		void exposesUncommittedValueToOtherTransactions() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);

				assertSoftly(softly -> {
					softly.assertThat(SeparateTransaction.read(() -> emailInDatabase()))
						 .as("DB 에서는 아직 커밋되지 않아 보이지 않는다")
						 .isEqualTo(ORIGINAL_EMAIL);
					softly.assertThat(SeparateTransaction.read(() -> userQueryService.findById(userId)).email())
						 .as("@CachePut 은 메서드 반환 시점에 적재하므로 바깥 커밋보다 먼저 캐시에 보인다")
						 .isEqualTo(CHANGED_EMAIL);
				});
			});
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션이 롤백되면")
	class WhenOuterTransactionRolledBack {

		@Test
		@DisplayName("DB 는 옛 값으로 돌아가고 캐시에는 새 값이 남는다")
		void leavesRolledBackValueInCache() {
			userQueryService.findById(userId);

			changeEmailThenRollBack();

			assertSoftly(softly -> {
				softly.assertThat(emailInDatabase())
					 .as("롤백으로 변경이 취소된다")
					 .isEqualTo(ORIGINAL_EMAIL);
				softly.assertThat(cachedEmail())
					 .as("적재는 이미 끝났고 롤백은 캐시를 되돌리지 않는다")
					 .isEqualTo(CHANGED_EMAIL);
			});
		}

		@Test
		@DisplayName("TTL 이 끝날 때까지 조회가 일어나지 않은 변경을 돌려준다")
		void keepsServingRolledBackValue() {
			userQueryService.findById(userId);

			changeEmailThenRollBack();

			assertThat(userQueryService.findById(userId).email())
				 .as("삭제 방식이면 롤백 비용이 MISS 1회지만, 갱신 방식은 틀린 값을 계속 제공한다")
				 .isEqualTo(CHANGED_EMAIL);
		}

		private void changeEmailThenRollBack() {
			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				status.setRollbackOnly();
			});
		}
	}

	private String cachedEmail() {
		UserView cached = cacheManager.getCache(CachePolicy.Name.USER)
			 .get(userId, UserView.class);
		return cached == null ? null : cached.email();
	}

	private String emailInDatabase() {
		return userRepository.findById(userId)
			 .map(User::getEmail)
			 .orElse(null);
	}
}

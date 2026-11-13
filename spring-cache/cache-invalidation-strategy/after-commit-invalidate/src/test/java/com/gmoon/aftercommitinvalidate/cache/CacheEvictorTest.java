package com.gmoon.aftercommitinvalidate.cache;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.aftercommitinvalidate.config.CachePolicy;
import com.gmoon.aftercommitinvalidate.user.User;
import com.gmoon.aftercommitinvalidate.user.NonTransactionalUserService;
import com.gmoon.aftercommitinvalidate.user.UserCommandService;
import com.gmoon.aftercommitinvalidate.user.UserQueryService;
import com.gmoon.aftercommitinvalidate.user.UserRepository;
import com.gmoon.aftercommitinvalidate.user.UserView;
import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.cacheinvalidation.test.SeparateTransaction;

@IntegrationTest
@DisplayName("커밋 이후 무효화")
class CacheEvictorTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";
	private static final String CHANGED_AGAIN_EMAIL = "alice.again@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired NonTransactionalUserService nonTransactionalUserService;
	@Autowired UserRepository userRepository;
	@Autowired TransactionTemplate transactionTemplate;
	@MockitoSpyBean CacheManager cacheManager;

	private Long userId;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(CachePolicy.Name.USER).clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
	}

	@Nested
	@DisplayName("변경이 커밋되면")
	class WhenChangeCommitted {

		@Test
		@DisplayName("옛 값을 지운다")
		void evictsStaleEntry() {
			userQueryService.findById(userId);

			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			assertThat(cachedEmail())
				 .as("AFTER_COMMIT 리스너가 쓰기 메서드 대신 삭제를 맡는다")
				 .isNull();
		}

		@Test
		@DisplayName("삭제된 사용자의 캐시도 지운다")
		void evictsEntryOfDeletedUser() {
			userQueryService.findById(userId);

			userCommandService.unregister(userId);

			assertThat(cachedEmail())
				 .as("삭제도 같은 이벤트로 알린다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("새 사용자를 등록하면")
	class WhenUserRegistered {

		@Test
		@DisplayName("커밋 전에 캐시된 '없음' 을 커밋 이후에 지운다")
		void evictsNotFoundCachedBeforeCommit() {
			Long registeredId = transactionTemplate.execute(status -> {
				Long id = userCommandService.register("bob-" + System.nanoTime(), ORIGINAL_EMAIL);
				SeparateTransaction.read(() -> userQueryService.findById(id));

				assertThat(cacheManager.getCache(CachePolicy.Name.USER).get(id))
					 .as("커밋 전 조회는 아직 보이지 않는 사용자를 '없음' 으로 캐시에 적재한다")
					 .isNotNull();
				return id;
			});

			assertThat(userQueryService.findById(registeredId))
				 .as("등록도 알리지 않으면 '없음' 이 TTL 동안 새 사용자를 가린다")
				 .isNotNull();
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션이 아직 커밋되지 않았으면")
	class WhenOuterTransactionStillOpen {

		@Test
		@DisplayName("캐시를 건드리지 않는다")
		void keepsCacheUntilCommit() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);

				assertThat(cachedEmail())
					 .as("확정되지 않은 변경은 삭제의 근거가 될 수 없다")
					 .isEqualTo(ORIGINAL_EMAIL);
			});
		}

		@Test
		@DisplayName("커밋 전에 끼어든 조회가 적재한 옛 값까지 걷어낸다")
		void evictsValueRepopulatedBeforeCommit() {
			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				SeparateTransaction.read(() -> userQueryService.findById(userId));

				assertThat(cachedEmail())
					 .as("커밋 전 조회는 MISS 후 아직 커밋되지 않은 옛 값을 적재한다")
					 .isEqualTo(ORIGINAL_EMAIL);
			});

			assertThat(cachedEmail())
				 .as("삭제가 커밋 뒤에 오므로 커밋 전에 심어진 값도 함께 사라진다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("바깥 트랜잭션이 롤백되면")
	class WhenOuterTransactionRolledBack {

		@Test
		@DisplayName("캐시를 지우지 않는다")
		void doesNotEvict() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				status.setRollbackOnly();
			});

			assertThat(cachedEmail())
				 .as("일어나지 않은 변경 때문에 캐시를 버리면 적중률만 떨어진다")
				 .isEqualTo(ORIGINAL_EMAIL);
		}
	}

	@Nested
	@DisplayName("트랜잭션 없이 변경을 발행하면")
	class WhenPublishedWithoutTransaction {

		@Test
		@DisplayName("이벤트가 폐기되어 DB 만 바뀌고 옛 값이 남는다")
		void discardsEventAndLeavesStaleEntry() {
			userQueryService.findById(userId);

			nonTransactionalUserService.changeEmail(userId, CHANGED_EMAIL);

			assertSoftly(softly -> {
				softly.assertThat(emailInDatabase())
					 .as("쓰기 자체는 성공한다")
					 .isEqualTo(CHANGED_EMAIL);
				softly.assertThat(cachedEmail())
					 .as("fallbackExecution 기본값이 false 라 트랜잭션 없는 발행은 조용히 버려진다")
					 .isEqualTo(ORIGINAL_EMAIL);
			});
		}

		@Test
		@DisplayName("같은 경로를 트랜잭션으로 감싸면 지워진다")
		void evictsWhenWrappedInTransaction() {
			userQueryService.findById(userId);

			transactionTemplate.executeWithoutResult(status -> nonTransactionalUserService.changeEmail(userId, CHANGED_EMAIL));

			assertThat(cachedEmail())
				 .as("누락의 원인은 배치 경로가 아니라 트랜잭션의 부재다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("같은 변경을 한 트랜잭션에서 두 번 알리면")
	class WhenSameChangeReportedTwice {

		@Test
		@DisplayName("삭제도 두 번 나간다")
		void evictsTwice() {
			Cache userCache = spy(cacheManager.getCache(CachePolicy.Name.USER));
			willReturn(userCache).given(cacheManager).getCache(CachePolicy.Name.USER);

			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				userCommandService.changeEmail(userId, CHANGED_AGAIN_EMAIL);
			});

			verify(userCache, times(2))
				 .evict(String.valueOf(userId));
		}
	}

	@Nested
	@DisplayName("같은 커밋의 앞 순번 콜백이 예외를 던져도")
	class WhenEarlierCallbackFails {

		@Test
		@DisplayName("캐시는 지워지고 호출자에게 예외가 전파되지 않는다")
		void stillEvictsWithoutPropagating() {
			userQueryService.findById(userId);
			AtomicReference<String> emailSeenByFailingCallback = new AtomicReference<>();

			assertThatNoException().isThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
				TransactionSynchronizationManager.registerSynchronization(failingCallback(emailSeenByFailingCallback));
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
			}));

			assertSoftly(softly -> {
				softly.assertThat(emailSeenByFailingCallback.get())
					 .as("터지는 콜백이 삭제보다 먼저 실행됐어야 이 명제가 성립한다")
					 .isEqualTo(ORIGINAL_EMAIL);
				softly.assertThat(cachedEmail())
					 .as("AFTER_COMMIT 은 afterCompletion 에서 실행되고 스프링이 콜백마다 예외를 잡는다")
					 .isNull();
			});
		}

		private TransactionSynchronization failingCallback(AtomicReference<String> emailAtFailure) {
			return new TransactionSynchronization() {

				@Override
				public int getOrder() {
					return Ordered.HIGHEST_PRECEDENCE;
				}

				@Override
				public void afterCompletion(int status) {
					emailAtFailure.set(cachedEmail());
					throw new IllegalStateException("earlier callback failed");
				}
			};
		}
	}

	@Nested
	@DisplayName("캐시 저장소가 죽어 있으면")
	class WhenCacheStoreIsDown {

		@Test
		@DisplayName("커밋은 성공하지만 무효화는 호출자 모르게 유실된다")
		void losesInvalidationSilently() {
			userQueryService.findById(userId);
			willThrow(new QueryTimeoutException("redis down"))
				 .given(cacheManager)
				 .getCache(CachePolicy.Name.USER);

			assertThatNoException()
				 .isThrownBy(() -> userCommandService.changeEmail(userId, CHANGED_EMAIL));

			reset(cacheManager);
			assertSoftly(softly -> {
				softly.assertThat(emailInDatabase())
					 .as("커밋은 이미 끝난 뒤라 되돌릴 수 없다")
					 .isEqualTo(CHANGED_EMAIL);
				softly.assertThat(cachedEmail())
					 .as("직접 지우는 리스너에는 실패를 세는 자리가 없다. TTL 이 만료될 때까지 틀린 값이 보인다")
					 .isEqualTo(ORIGINAL_EMAIL);
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

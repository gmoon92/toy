package com.gmoon.writeinvalidate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.writeinvalidate.user.UserCachePolicy;
import com.gmoon.writeinvalidate.user.UserCommandService;
import com.gmoon.writeinvalidate.user.UserQueryService;

@IntegrationTest
@DisplayName("무효화 실패가 트랜잭션에 미치는 영향")
class InvalidationFailureTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired TransactionTemplate transactionTemplate;
	@MockitoSpyBean CacheManager cacheManager;

	private Long userId;

	@BeforeEach
	void setUp() {
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
	}

	@Nested
	@DisplayName("캐시 인프라가 예기치 못한 예외를 던지면")
	class WhenCacheInfrastructureFails {

		@Test
		@DisplayName("커밋은 성공하고 호출자에게 예외가 새어나가지 않는다")
		void keepsCommitSuccessfulWithoutLeakingException() {
			willThrow(new QueryTimeoutException("redis down"))
				 .given(cacheManager).getCache(UserCachePolicy.Name.USER);

			assertThatNoException()
				 .as("무효화 실패는 이미 커밋된 트랜잭션을 되돌릴 수 없다. 예외를 밖으로 내면 호출자가 롤백으로 오해한다")
				 .isThrownBy(() -> transactionTemplate.executeWithoutResult(status ->
					  userCommandService.changeEmail(userId, CHANGED_EMAIL)));
		}

		@Test
		@DisplayName("DB 변경은 그대로 커밋되어 있다")
		void persistsDatabaseChange() {
			willThrow(new QueryTimeoutException("redis down"))
				 .given(cacheManager).getCache(UserCachePolicy.Name.USER);

			try {
				transactionTemplate.executeWithoutResult(status ->
					 userCommandService.changeEmail(userId, CHANGED_EMAIL));
			} catch (RuntimeException ignored) {
				// 결함이 남아 있는 동안에도 DB 상태를 확인하기 위해 통과시킨다
			}

			reset(cacheManager);
			assertThat(userQueryService.findById(userId).email())
				 .as("커밋은 물리적으로 끝난 뒤에 무효화가 실행되므로 DB 는 영향받지 않는다")
				 .isEqualTo(CHANGED_EMAIL);
		}
	}
}

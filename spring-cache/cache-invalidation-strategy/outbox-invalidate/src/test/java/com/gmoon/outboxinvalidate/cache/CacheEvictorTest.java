package com.gmoon.outboxinvalidate.cache;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.QueryTimeoutException;

import com.gmoon.outboxinvalidate.OutboxIntegrationTest;
import com.gmoon.outboxinvalidate.config.CachePolicy;
import com.gmoon.outboxinvalidate.user.UserCommandService;
import com.gmoon.outboxinvalidate.user.UserQueryService;
import com.gmoon.outboxinvalidate.user.UserView;

@OutboxIntegrationTest
@DisplayName("브로커에서 받은 무효화 지시 처리")
class CacheEvictorTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";
	private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration REDELIVERY_WINDOW = Duration.ofSeconds(1);

	@Autowired EvictionRelay evictionRelay;
	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired PendingEvictionRepository pendingEvictionRepository;
	@Autowired CacheManager cacheManager;

	private Cache userCache;
	private Long userId;

	@BeforeEach
	void setUp() {
		userCache = cacheManager.getCache(CachePolicy.Name.USER);
		userCache.clear();
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
		pendingEvictionRepository.deleteAllInBatch();

		userQueryService.findById(userId);
		userCommandService.changeEmail(userId, CHANGED_EMAIL);
		clearInvocations(cacheManager);
	}

	@Nested
	@DisplayName("캐시 저장소가 잠시 실패하면")
	class WhenCacheStoreFailsOnce {

		@Test
		@DisplayName("지시를 다시 받아 결국 지운다")
		void redeliversUntilEvicted() {
			willThrow(new QueryTimeoutException("redis down"))
				 .willCallRealMethod()
				 .given(cacheManager)
				 .getCache(CachePolicy.Name.USER);

			evictionRelay.relay();

			await().atMost(DELIVERY_TIMEOUT)
				 .untilAsserted(() -> assertThat(cachedEmail())
					  .as("처리하지 못한 지시는 큐로 돌아가 다시 온다. 직접 지우는 방식은 이 실패에서 무효화를 잃는다")
					  .isNull());
			then(cacheManager).should(atLeast(2)).getCache(CachePolicy.Name.USER);
		}
	}

	@Nested
	@DisplayName("정책에 없는 캐시를 지목하면")
	class WhenCacheNotRegistered {

		@Test
		@DisplayName("다시 받지 않고 버린다")
		void dropsWithoutRedelivery() {
			willReturn(null)
				 .given(cacheManager)
				 .getCache(CachePolicy.Name.USER);

			evictionRelay.relay();

			await().during(REDELIVERY_WINDOW)
				 .atMost(DELIVERY_TIMEOUT)
				 .untilAsserted(() -> then(cacheManager)
					  .should(description("몇 번을 다시 받아도 처리할 수 없는 지시가 큐를 끝없이 돌면 안 된다"))
					  .getCache(CachePolicy.Name.USER));
		}
	}

	private String cachedEmail() {
		UserView cached = userCache.get(userId, UserView.class);
		return cached == null ? null : cached.email();
	}
}

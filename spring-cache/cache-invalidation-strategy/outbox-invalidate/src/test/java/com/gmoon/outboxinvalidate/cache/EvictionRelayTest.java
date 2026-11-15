package com.gmoon.outboxinvalidate.cache;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.BDDMockito.*;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.outboxinvalidate.OutboxIntegrationTest;
import com.gmoon.outboxinvalidate.config.CachePolicy;
import com.gmoon.outboxinvalidate.user.UserCommandService;
import com.gmoon.outboxinvalidate.user.UserQueryService;
import com.gmoon.outboxinvalidate.user.UserView;

@OutboxIntegrationTest
@DisplayName("무효화 지시 릴레이")
class EvictionRelayTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";
	private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(5);

	@Autowired EvictionRelay evictionRelay;
	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired PendingEvictionRepository pendingEvictionRepository;
	@Autowired TransactionTemplate transactionTemplate;
	@Autowired CacheManager cacheManager;
	@Autowired RabbitTemplate rabbitTemplate;

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
	}

	@Nested
	@DisplayName("커밋 직후 앱이 죽어 릴레이가 돌지 않았으면")
	class WhenCommittedButNotRelayed {

		@Test
		@DisplayName("옛 값이 보이지만 지시는 DB 에 남는다")
		void keepsInstructionInDatabase() {
			assertSoftly(softly -> {
				softly.assertThat(cachedEmail())
					 .as("릴레이가 돌기 전까지는 옛 값이 보인다. outbox 는 즉시성을 보장하지 않는다")
					 .isEqualTo(ORIGINAL_EMAIL);
				softly.assertThat(pendingKeys())
					 .as("지시는 메모리가 아니라 테이블에 있으므로 프로세스가 죽어도 사라지지 않는다")
					 .containsExactly(userKey());
			});
		}
	}

	@Nested
	@DisplayName("재기동한 릴레이가 대기 행을 옮기면")
	class WhenRelayedAfterRestart {

		@Test
		@DisplayName("캐시를 지우고 대기 행을 비운다")
		void evictsAndClearsPendingRows() {
			evictionRelay.relay();

			awaitEvicted();
			assertThat(pendingKeys())
				 .as("브로커가 받았다고 확인한 지시는 다시 보내지 않는다")
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("브로커가 죽어 있으면")
	class WhenBrokerDown {

		@Test
		@DisplayName("대기 행을 남겼다가 다음 주기에 보낸다")
		void keepsRowsUntilNextRelay() {
			willThrow(new AmqpConnectException(new ConnectException("broker down")))
				 .given(rabbitTemplate)
				 .invoke(any());

			assertThatExceptionOfType(AmqpConnectException.class)
				 .isThrownBy(evictionRelay::relay);
			assertThat(pendingKeys())
				 .as("보내지 못한 지시를 지우면 무효화가 유실된다")
				 .containsExactly(userKey());

			reset(rabbitTemplate);
			evictionRelay.relay();

			awaitEvicted();
		}
	}

	@Nested
	@DisplayName("보낸 뒤 대기 행 삭제를 커밋하지 못하면")
	class WhenRowDeletionNotCommitted {

		@Test
		@DisplayName("같은 지시를 다시 보낸다")
		void sendsSameInstructionAgain() {
			relayWithoutCommittingDeletion();
			awaitEvicted();

			assertThat(pendingKeys())
				 .as("브로커는 이미 받았지만 행이 남아 다음 릴레이가 또 보낸다. 정확히 한 번이 아니라 최소 한 번이다")
				 .containsExactly(userKey());
		}

		@Test
		@DisplayName("다시 보낸 지시는 MISS 를 한 번 더 만들 뿐 옛 값을 되살리지 않는다")
		void duplicateEvictionKeepsLatestValue() {
			relayWithoutCommittingDeletion();
			awaitEvicted();
			userQueryService.findById(userId);

			evictionRelay.relay();
			awaitEvicted();

			assertThat(userQueryService.findById(userId).email())
				 .as("캐시 삭제는 몇 번을 해도 결과가 같아 중복을 걸러 낼 필요가 없다")
				 .isEqualTo(CHANGED_EMAIL);
		}

		private void relayWithoutCommittingDeletion() {
			transactionTemplate.executeWithoutResult(status -> {
				evictionRelay.relay();
				status.setRollbackOnly();
			});
		}
	}

	private void awaitEvicted() {
		await().atMost(DELIVERY_TIMEOUT)
			 .untilAsserted(() -> assertThat(cachedEmail())
				  .as("브로커를 거친 지시가 소비자에게 닿으면 캐시가 지워진다")
				  .isNull());
	}

	private CacheKey userKey() {
		return CacheKey.of(CachePolicy.USER, userId);
	}

	private String cachedEmail() {
		UserView cached = userCache.get(userId, UserView.class);
		return cached == null ? null : cached.email();
	}

	private List<CacheKey> pendingKeys() {
		return pendingEvictionRepository.findAll().stream()
			 .map(PendingEviction::cacheKey)
			 .toList();
	}
}

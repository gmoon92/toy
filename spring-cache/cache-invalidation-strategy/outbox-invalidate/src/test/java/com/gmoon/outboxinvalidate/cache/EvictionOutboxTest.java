package com.gmoon.outboxinvalidate.cache;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmoon.outboxinvalidate.OutboxIntegrationTest;
import com.gmoon.outboxinvalidate.config.CachePolicy;
import com.gmoon.outboxinvalidate.user.User;
import com.gmoon.outboxinvalidate.user.UserCommandService;
import com.gmoon.outboxinvalidate.user.UserRepository;

@OutboxIntegrationTest
@DisplayName("무효화 지시 기록")
class EvictionOutboxTest {

	private static final String ORIGINAL_EMAIL = "alice@mail.com";
	private static final String CHANGED_EMAIL = "alice.changed@mail.com";

	@Autowired EvictionOutbox evictionOutbox;
	@Autowired UserCommandService userCommandService;
	@Autowired UserRepository userRepository;
	@Autowired PendingEvictionRepository pendingEvictionRepository;
	@Autowired TransactionTemplate transactionTemplate;

	private Long userId;

	@BeforeEach
	void setUp() {
		userId = userCommandService.register("alice-" + System.nanoTime(), ORIGINAL_EMAIL);
		pendingEvictionRepository.deleteAllInBatch();
	}

	@Nested
	@DisplayName("변경이 커밋되면")
	class WhenChangeCommitted {

		@Test
		@DisplayName("지울 키를 대기 행으로 남긴다")
		void recordsKeyAsPendingRow() {
			userCommandService.changeEmail(userId, CHANGED_EMAIL);

			assertThat(pendingKeys())
				 .as("변경과 같은 트랜잭션에 적었으므로 커밋되면 지시도 함께 남는다")
				 .containsExactly(userKey());
		}

		@Test
		@DisplayName("삭제된 사용자의 키도 남긴다")
		void recordsKeyOfDeletedUser() {
			userCommandService.unregister(userId);

			assertThat(pendingKeys()).containsExactly(userKey());
		}
	}

	@Nested
	@DisplayName("새 사용자를 등록하면")
	class WhenUserRegistered {

		@Test
		@DisplayName("지울 키를 남긴다")
		void recordsKeyOfRegisteredUser() {
			Long registeredId = userCommandService.register("bob-" + System.nanoTime(), ORIGINAL_EMAIL);

			assertThat(pendingKeys())
				 .as("커밋 전 조회가 캐시한 '없음' 을 지워야 한다")
				 .containsExactly(CacheKey.of(CachePolicy.USER, registeredId));
		}
	}

	@Nested
	@DisplayName("변경이 롤백되면")
	class WhenChangeRolledBack {

		@Test
		@DisplayName("대기 행도 남지 않는다")
		void leavesNoPendingRow() {
			transactionTemplate.executeWithoutResult(status -> {
				userCommandService.changeEmail(userId, CHANGED_EMAIL);
				status.setRollbackOnly();
			});

			assertThat(pendingKeys())
				 .as("일어나지 않은 변경의 지시가 남으면 캐시를 괜히 비워 적중률만 떨어진다")
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("트랜잭션 없이 기록하면")
	class WhenRecordedWithoutTransaction {

		@Test
		@DisplayName("예외로 거부한다")
		void rejects() {
			User user = userRepository.findById(userId).orElseThrow();

			assertThatExceptionOfType(IllegalTransactionStateException.class)
				 .as("따로 커밋되면 변경만 남고 지시가 사라지는 틈이 생긴다. 조용히 넘기지 않고 호출자에게 알린다")
				 .isThrownBy(() -> evictionOutbox.record(user));
		}
	}

	private CacheKey userKey() {
		return CacheKey.of(CachePolicy.USER, userId);
	}

	private List<CacheKey> pendingKeys() {
		return pendingEvictionRepository.findAll().stream()
			 .map(PendingEviction::cacheKey)
			 .toList();
	}
}

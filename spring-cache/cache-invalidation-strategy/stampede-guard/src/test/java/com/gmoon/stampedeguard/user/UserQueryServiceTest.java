package com.gmoon.stampedeguard.user;

import static com.gmoon.stampedeguard.ConcurrentReaders.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.gmoon.cacheinvalidation.test.IntegrationTest;
import com.gmoon.stampedeguard.config.CachePolicy;

@IntegrationTest
@DisplayName("사용자 조회")
class UserQueryServiceTest {

	private static final int CONCURRENT_READERS = 8;
	private static final Duration SLOW_LOAD = Duration.ofMillis(500);
	private static final String EMAIL = "alice@mail.com";

	@Autowired UserQueryService userQueryService;
	@Autowired UserCommandService userCommandService;
	@Autowired CacheManager cacheManager;
	@MockitoSpyBean UserRepository userRepository;

	private Long userId;

	@BeforeEach
	void setUp() {
		userId = userCommandService.register("alice-" + System.nanoTime(), EMAIL);
		Answer<?> delegateToRepository = mockingDetails(userRepository).getMockCreationSettings().getDefaultAnswer();
		willAnswer(invocation -> {
			pause(SLOW_LOAD);
			return delegateToRepository.answer(invocation);
		}).given(userRepository).findById(userId);
	}

	@Nested
	@DisplayName("무효화 직후 같은 사용자 조회가 몰리면")
	class WhenReadersMissTogether {

		@Test
		@DisplayName("DB 는 한 번만 읽는다")
		void readsDatabaseOnce() {
			invalidate();

			readTogether(CONCURRENT_READERS, () -> userQueryService.findById(userId));

			then(userRepository).should(times(1).description(
				 "락을 기다린 요청은 락이 풀린 뒤 이미 적재된 값을 읽는다")).findById(userId);
		}

		@Test
		@DisplayName("모든 요청이 같은 값을 받는다")
		void everyReaderGetsSameValue() {
			invalidate();

			List<UserView> results = readTogether(CONCURRENT_READERS, () -> userQueryService.findById(userId));

			assertThat(results)
				 .as("원본을 읽지 않은 요청도 빈손이 아니라 적재된 값을 돌려받는다")
				 .extracting(UserView::email)
				 .containsOnly(EMAIL);
		}
	}

	private void invalidate() {
		cacheManager.getCache(CachePolicy.Name.USER).evict(userId);
	}
}

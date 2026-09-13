package com.gmoon.cacheinvalidation.core.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRecorder;

/**
 * 리스너가 실제로 동작하는지는 통합 테스트({@code JpaEntityChangeTest})가 증명한다.
 * 여기서는 통합 테스트가 잡지 못하는 기동 시점의 구멍만 고정한다.
 */
@DisplayName("JPA 변경 감지 설치")
class JpaEntityChangeConfigTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		 .withBean(CacheInvalidator.class, () -> mock(CacheInvalidator.class))
		 .withBean(InvalidationRecorder.class, InvalidationRecorder::new)
		 .withUserConfiguration(JpaEntityChangeConfig.class);

	@Nested
	@DisplayName("EntityManagerFactory 가 없는데 감지를 켜면")
	class WhenNoEntityManagerFactory {

		@Test
		@DisplayName("기동을 중단한다")
		void failsFast() {
			contextRunner.run(context -> assertThat(context)
				 .as("조용히 뜨면 무효화가 죽은 채로 운영에 올라간다")
				 .hasFailed());
		}

		@Test
		@DisplayName("무엇이 없어서 못 떴는지 메시지로 알린다")
		void explainsWhatIsMissing() {
			contextRunner.run(context -> assertThat(context)
				 .getFailure()
				 .rootCause()
				 .as("원인을 안 밝히면 설정 실수를 찾는 데 시간이 든다")
				 .hasMessageContaining("EntityManagerFactory"));
		}
	}
}

package com.gmoon.cacheinvalidation.core.invalidation;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;

@DisplayName("커밋 이후의 무효화 실패")
class FailSafeCacheInvalidatorTest {

	private static final ChangeSource SOURCE = ChangeSource.JPA_ENTITY;

	private final InvalidationRecorder recorder = new InvalidationRecorder();

	@Nested
	@DisplayName("무효화가 예외를 던지면")
	class WhenInvalidationThrows {

		private final CacheInvalidator invalidator = new FailSafeCacheInvalidator(
			 (change, source) -> {
				 throw new IllegalStateException("redis down");
			 }, recorder);

		@Test
		@DisplayName("호출자에게 예외를 돌려주지 않는다")
		void doesNotPropagate() {
			assertThatNoException()
				 .as("신호는 커밋 이후에 도착하므로 예외가 새면 이미 끝난 트랜잭션의 호출자가 깨진다")
				 .isThrownBy(() -> invalidator.invalidate(EntityChange.inserted(new Object()), SOURCE));
		}

		@Test
		@DisplayName("삼킨 실패를 신호 소스별로 남긴다")
		void recordsFailureBySource() {
			invalidator.invalidate(EntityChange.inserted(new Object()), SOURCE);

			assertThat(recorder.pipelineFailureCount(SOURCE))
				 .as("조용히 삼키기만 하면 무효화 누락을 알아챌 방법이 없다")
				 .isOne();
			assertThat(recorder.pipelineFailureCount(ChangeSource.APPLICATION_EVENT))
				 .as("실패한 소스만 세어야 어느 경로가 깨졌는지 가려진다")
				 .isZero();
		}
	}

	@Nested
	@DisplayName("무효화가 정상이면")
	class WhenInvalidationSucceeds {

		@Test
		@DisplayName("변경과 소스를 그대로 위임한다")
		void delegatesUnchanged() {
			List<EntityChange> delegated = new ArrayList<>();
			CacheInvalidator invalidator = new FailSafeCacheInvalidator(
				 (change, source) -> delegated.add(change), recorder);
			EntityChange change = EntityChange.inserted(new Object());

			invalidator.invalidate(change, SOURCE);

			assertThat(delegated)
				 .as("감싸는 과정에서 신호가 바뀌거나 사라지면 안 된다")
				 .containsExactly(change);
			assertThat(recorder.pipelineFailureCount(SOURCE))
				 .as("성공을 실패로 세면 지표가 거짓말을 한다")
				 .isZero();
		}
	}
}

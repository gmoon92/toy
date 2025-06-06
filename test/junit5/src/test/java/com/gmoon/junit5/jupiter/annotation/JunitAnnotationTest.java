package com.gmoon.junit5.jupiter.annotation;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class JunitAnnotationTest {

	@DisplayName("Junit timeout test")
	@Nested
	class TimeoutTest {

		@DisplayName("@Timeout 어노테이션 기반 테스트 " +
			 "비동기 방식으로 응답 시간 검증")
		@Timeout(
			 value = 1,
			 unit = TimeUnit.SECONDS // default SECONDS
		)
		@Test
		void timeoutAnnotation() {
			slowQuery();
		}

		@DisplayName("assertTimeout() " +
			 "동기 방식으로 응답 시간 검증" +
			 "1. 단언문 두번째 인자의 executable 메서드 호출" +
			 "2. 호출된 시간을 측정하고 시간 검증" +
			 "예를 들어 검증할 메서드 실행 시간이 5초가 걸렸다고 하면, " +
			 "검증 시간이 1초라도 5초 이후에 검증")
		@Test
		void assertTimeout() {
			Assertions.assertTimeout(
				 Duration.ofSeconds(1),
				 this::slowQuery
			);
		}

		@DisplayName("assertTimeoutPreemptively() " +
			 "비동기 방식으로 응답 시간 검증" +
			 "Future 를 사용하여 비동기 방식으로 시간 검증")
		@Test
		void assertTimeoutPreemptively() {
			Assertions.assertTimeoutPreemptively(
				 Duration.ofSeconds(1),
				 this::slowQuery
			);
		}

		void slowQuery() {
			try {
				Thread.sleep(Duration.ofMillis(5).toMillis());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

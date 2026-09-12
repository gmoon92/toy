package com.gmoon.ttlonly.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.listener.JpaEntityChangeListener;
import com.gmoon.cacheinvalidation.core.listener.PublishedEntityChangeListener;
import com.gmoon.cacheinvalidation.test.IntegrationTest;

@IntegrationTest
@DisplayName("TTL 만 쓰는 모듈의 설정")
class CacheConfigTest {

	@Autowired ApplicationContext context;

	@Nested
	@DisplayName("변경 감지 방식을 선언하지 않으면")
	class WhenNoChangeDetectionDeclared {

		@Test
		@DisplayName("Hibernate 리스너가 등록되지 않는다")
		void doesNotRegisterHibernateListener() {
			assertThat(context.getBeanNamesForType(JpaEntityChangeListener.class))
				 .as("쓰지 않는 전략의 빈까지 떠안으면 코어가 모듈을 무겁게 만든다")
				 .isEmpty();
		}

		@Test
		@DisplayName("Spring 이벤트 리스너가 등록되지 않는다")
		void doesNotRegisterSpringListener() {
			assertThat(context.getBeanNamesForType(PublishedEntityChangeListener.class))
				 .isEmpty();
		}
	}

	@Nested
	@DisplayName("변경 감지가 없어도")
	class EvenWithoutChangeDetection {

		@Test
		@DisplayName("무효화 파이프라인은 그대로 조립된다")
		void stillAssemblesInvalidationPipeline() {
			assertThat(context.getBeanNamesForType(CacheInvalidator.class))
				 .as("파이프라인은 코어가 확정하고, 무엇으로 신호를 줄지만 모듈이 고른다")
				 .hasSize(1);
		}
	}
}

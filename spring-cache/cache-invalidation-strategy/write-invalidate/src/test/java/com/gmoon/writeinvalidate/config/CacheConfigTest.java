package com.gmoon.writeinvalidate.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.gmoon.cacheinvalidation.core.cache.expiration.CacheExpiration;
import com.gmoon.cacheinvalidation.core.cache.serialization.CacheSerialization;
import com.gmoon.cacheinvalidation.core.invalidation.listener.JpaEntityChangeListener;
import com.gmoon.cacheinvalidation.core.invalidation.listener.PublishedEntityChangeListener;
import com.gmoon.cacheinvalidation.test.IntegrationTest;

@IntegrationTest
@DisplayName("두 감지 방식을 모두 켠 모듈의 설정")
class CacheConfigTest {

	@Autowired ApplicationContext context;

	@Nested
	@DisplayName("선언한 감지 방식은")
	class DeclaredChangeDetections {

		@Test
		@DisplayName("모두 등록된다")
		void areAllRegistered() {
			assertThat(context.getBeanNamesForType(JpaEntityChangeListener.class)).hasSize(1);
			assertThat(context.getBeanNamesForType(PublishedEntityChangeListener.class)).hasSize(1);
		}
	}

	@Nested
	@DisplayName("직렬화와 만료 전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		@Test
		@DisplayName("코어의 기본 구현이 쓰인다")
		void fallBackToCoreDefaults() {
			assertThat(context.getBean(CacheSerialization.class))
				 .as("모듈이 재정의하지 않아도 기본 전략으로 동작해야 한다")
				 .isNotNull();
			assertThat(context.getBean(CacheExpiration.class)).isNotNull();
		}
	}
}

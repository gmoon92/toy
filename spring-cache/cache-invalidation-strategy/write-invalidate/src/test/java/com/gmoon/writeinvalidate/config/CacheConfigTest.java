package com.gmoon.writeinvalidate.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.gmoon.cacheinvalidation.core.cache.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.cache.serialization.SerializerFactory;
import com.gmoon.cacheinvalidation.core.config.listener.JpaEntityChangeConfig;
import com.gmoon.cacheinvalidation.core.config.listener.PublishedEntityChangeListener;
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
			assertThat(context.getBeanNamesForType(JpaEntityChangeConfig.class))
				 .as("JPA 엔티티 변경 감지. 리스너는 하이버네이트가 소유하므로 설정 존재로 확인한다")
				 .hasSize(1);
			assertThat(context.getBeanNamesForType(PublishedEntityChangeListener.class))
				 .as("애플리케이션 이벤트 감지. 이쪽은 스프링이 리스너를 찾아야 하므로 빈이어야 한다")
				 .hasSize(1);
		}
	}

	@Nested
	@DisplayName("직렬화와 만료 전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		@Test
		@DisplayName("코어의 기본 구현이 쓰인다")
		void fallBackToCoreDefaults() {
			assertThat(context.getBean(SerializerFactory.class))
				 .as("모듈이 재정의하지 않아도 기본 전략으로 동작해야 한다")
				 .isNotNull();
			assertThat(context.getBean(TtlResolver.class))
				 .as("만료 전략도 기본 구현으로 채워져야 한다")
				 .isNotNull();
		}
	}
}

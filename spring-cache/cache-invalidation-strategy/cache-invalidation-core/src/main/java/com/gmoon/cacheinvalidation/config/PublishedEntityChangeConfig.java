package com.gmoon.cacheinvalidation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmoon.cacheinvalidation.CacheInvalidator;

/**
 * 애플리케이션이 직접 발행한 변경을 무효화 신호로 삼는다.
 * <p>
 * JPA 를 거치지 않는 쓰기(JDBC, jOOQ, 벌크 연산, 외부 연동)까지 덮을 수 있는 대신,
 * 발행하는 코드가 누락되면 무효화도 함께 누락된다.
 * <p>
 * 커밋 이후에만 실행된다 — 리스너가 {@code @TransactionalEventListener(AFTER_COMMIT)} 이다.
 * 이전 상태는 싣지 않으므로, 옛 키를 재구성하는 규칙은 이 경로에서 동작하지 않는다.
 * <p>
 * {@link JpaEntityChangeConfig} 와 함께 켜더라도 <b>같은 변경을 두 경로로 알리지 않는다.</b>
 * 둘은 서로 다른 쓰기를 덮으라고 있는 것이고, 같은 변경을 중복으로 알리면 캐시를 두 번 지운다.
 */
@Configuration
public class PublishedEntityChangeConfig {

	@Bean
	public PublishedEntityChangeListener publishedEntityChangeListener(CacheInvalidator cacheInvalidator) {
		return new PublishedEntityChangeListener(cacheInvalidator);
	}
}

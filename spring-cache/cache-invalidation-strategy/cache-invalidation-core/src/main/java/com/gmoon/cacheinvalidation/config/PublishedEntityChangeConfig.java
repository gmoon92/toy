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
 * 신호는 커밋 직전에 받지만 캐시를 지우는 것은 커밋 이후다. 롤백되면 수집분을 버린다.
 * 이전 상태는 기본으로 비어 있다 — 옛 키를 재구성해야 한다면
 * {@link EntityChange#of(Object, EntityChange.Type, EntityChange.State)} 로 실어 보낸다.
 * <p>
 * {@link JpaEntityChangeConfig} 와 함께 켜도 같은 키는 한 번만 지워진다.
 * 한 트랜잭션의 무효화 대상은 모였다가 커밋 이후 한 번에 처리된다.
 */
@Configuration
public class PublishedEntityChangeConfig {

	@Bean
	public PublishedEntityChangeListener publishedEntityChangeListener(CacheInvalidator cacheInvalidator) {
		return new PublishedEntityChangeListener(cacheInvalidator);
	}
}

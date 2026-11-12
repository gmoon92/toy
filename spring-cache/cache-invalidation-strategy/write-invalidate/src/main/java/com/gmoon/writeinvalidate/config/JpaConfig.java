package com.gmoon.writeinvalidate.config;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

import com.gmoon.writeinvalidate.cache.CacheEvictListener;

import lombok.RequiredArgsConstructor;

/**
 * 하이버네이트 엔티티 이벤트를 캐시 무효화에 잇는다.
 */
@Configuration
@RequiredArgsConstructor
public class JpaConfig {

	private final EntityManagerFactory entityManagerFactory;
	private final CacheEvictListener cacheEvictListener;

	/**
	 * 하이버네이트 리스너는 스프링이 아니라 하이버네이트의 {@link EventListenerRegistry} 에 붙여야 호출된다.
	 */
	@PostConstruct
	public void registerCacheEvictListener() {
		EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
			 .getServiceRegistry()
			 .getService(EventListenerRegistry.class);

		registry.appendListeners(EventType.POST_INSERT, cacheEvictListener);
		registry.appendListeners(EventType.POST_UPDATE, cacheEvictListener);
		registry.appendListeners(EventType.POST_DELETE, cacheEvictListener);
	}
}

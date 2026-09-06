package com.gmoon.cacheinvalidation.core.config;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;

import com.gmoon.cacheinvalidation.core.event.CacheEvictEventListener;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheEvictEventListenerRegistrar {

	private final EntityManagerFactory entityManagerFactory;
	private final CacheEvictEventListener cacheEvictEventListener;

	@PostConstruct
	public void register() {
		SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry()
			 .getService(EventListenerRegistry.class);

		if (registry == null) {
			throw new IllegalStateException("EventListenerRegistry is not available");
		}

		registry.appendListeners(EventType.POST_COMMIT_INSERT, cacheEvictEventListener);
		registry.appendListeners(EventType.POST_COMMIT_UPDATE, cacheEvictEventListener);
		registry.appendListeners(EventType.POST_COMMIT_DELETE, cacheEvictEventListener);
	}
}

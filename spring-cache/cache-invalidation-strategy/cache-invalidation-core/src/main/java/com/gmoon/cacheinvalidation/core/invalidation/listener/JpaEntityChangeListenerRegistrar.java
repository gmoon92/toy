package com.gmoon.cacheinvalidation.core.invalidation.listener;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.ObjectProvider;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

/**
 * 스프링이 만든 리스너를 하이버네이트의 이벤트 레지스트리에 설치한다.
 * <p>
 * 하이버네이트는 스프링 빈을 알지 못하고 자기 {@link EventListenerRegistry} 에 등록된 리스너만 호출한다.
 * 이 설치가 없으면 리스너 빈은 존재하지만 한 번도 불리지 않는다.
 */
@RequiredArgsConstructor
public class JpaEntityChangeListenerRegistrar {

	private final ObjectProvider<EntityManagerFactory> entityManagerFactories;
	private final JpaEntityChangeListener listener;

	@PostConstruct
	public void register() {
		long registered = entityManagerFactories.stream()
			 .peek(this::registerTo)
			 .count();

		if (registered == 0) {
			throw new IllegalStateException("No EntityManagerFactory found to register cache invalidation listener");
		}
	}

	private void registerTo(EntityManagerFactory entityManagerFactory) {
		EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImpl.class)
			 .getServiceRegistry()
			 .getService(EventListenerRegistry.class);

		if (registry == null) {
			throw new IllegalStateException("EventListenerRegistry is not available");
		}

		registry.appendListeners(EventType.POST_COMMIT_INSERT, listener);
		registry.appendListeners(EventType.POST_COMMIT_UPDATE, listener);
		registry.appendListeners(EventType.POST_COMMIT_DELETE, listener);
	}
}

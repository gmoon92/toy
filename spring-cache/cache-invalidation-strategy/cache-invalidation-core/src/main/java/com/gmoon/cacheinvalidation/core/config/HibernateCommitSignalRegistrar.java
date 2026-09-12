package com.gmoon.cacheinvalidation.core.config;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.ObjectProvider;

import com.gmoon.cacheinvalidation.core.event.HibernateCommitSignalListener;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HibernateCommitSignalRegistrar {

	private final ObjectProvider<EntityManagerFactory> entityManagerFactories;
	private final HibernateCommitSignalListener listener;

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

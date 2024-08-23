package com.gmoon.hibernateenvers.global.config;

import java.util.Objects;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.gmoon.hibernateenvers.global.envers.listener.RevisionHistoryEventListener;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.gmoon.**",
	 repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EnableTransactionManagement
@RequiredArgsConstructor
public class JpaConfig {

	private final EntityManagerFactory entityManagerFactory;

	@PostConstruct
	public void init() {
		SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
		EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

		Objects.requireNonNull(registry).appendListeners(EventType.POST_COMMIT_INSERT, new RevisionHistoryEventListener());
	}
}

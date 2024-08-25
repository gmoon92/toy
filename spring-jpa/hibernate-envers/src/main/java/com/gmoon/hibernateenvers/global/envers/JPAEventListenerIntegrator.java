package com.gmoon.hibernateenvers.global.envers;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import com.gmoon.hibernateenvers.global.config.JpaConfig;

/**
 * @deprecated As of release since 6.0, using by {@link JpaConfig#init()}
 */
@Deprecated(since = "6.0", forRemoval = true)
public class JPAEventListenerIntegrator implements Integrator {

	@Override
	public void integrate(
		 Metadata metadata,
		 SessionFactoryImplementor sessionFactory,
		 SessionFactoryServiceRegistry serviceRegistry
	) {
		//
		// final EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
		// EntityManager em = sessionFactory.createEntityManager();
		// listenerRegistry.appendListeners(EventType.POST_COMMIT_INSERT, new RevisionHistoryEventListener());
	}

	@Override
	public void disintegrate(
		 SessionFactoryImplementor sessionFactoryImplementor,
		 SessionFactoryServiceRegistry sessionFactoryServiceRegistry
	) {
	}
}

package com.gmoon.hibernateenvers.global.envers;

import com.gmoon.hibernateenvers.global.envers.listener.RevisionHistoryEventListener;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import javax.persistence.EntityManager;

@Slf4j
public class JPAEventListenerIntegrator implements Integrator {

  @Override
  public void integrate(Metadata metadata,
                        SessionFactoryImplementor sessionFactory,
                        SessionFactoryServiceRegistry serviceRegistry) {
    final EventListenerRegistry listenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
    EntityManager em = sessionFactory.createEntityManager();
    listenerRegistry.appendListeners(EventType.POST_COMMIT_INSERT, new RevisionHistoryEventListener(em));
  }

  @Override
  public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {

  }
}

package com.gmoon.hibernateenvers.global.config;

import com.gmoon.hibernateenvers.global.listener.RevisionHistoryEventListener;
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
  public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    final EventListenerRegistry registry = sessionFactoryServiceRegistry.getService(EventListenerRegistry.class);

    log.debug("sessionFactoryImplementor : {}", sessionFactoryImplementor);
    EntityManager em = sessionFactoryImplementor.createEntityManager();
    registry.appendListeners(EventType.POST_COMMIT_INSERT, new RevisionHistoryEventListener(em));
  }

  @Override
  public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {

  }

}

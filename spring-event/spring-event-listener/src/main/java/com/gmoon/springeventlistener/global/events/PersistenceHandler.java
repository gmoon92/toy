package com.gmoon.springeventlistener.global.events;

import javax.persistence.EntityManager;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersistenceHandler {

	private final EntityManager em;

	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(PostInsertEvent<?> event) {
		Object entity = event.getSource();
		em.persist(entity);
	}
}

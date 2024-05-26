package com.gmoon.hibernateannotation.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
public abstract class BaseRepositoryTest {

	protected Logger log = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	private EntityManager entityManager;

	public void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}

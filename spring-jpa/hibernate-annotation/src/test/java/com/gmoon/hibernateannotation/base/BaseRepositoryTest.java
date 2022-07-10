package com.gmoon.hibernateannotation.base;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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

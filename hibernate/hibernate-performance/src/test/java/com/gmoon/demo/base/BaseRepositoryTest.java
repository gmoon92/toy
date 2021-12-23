package com.gmoon.demo.base;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import com.gmoon.demo.config.QueryDslConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;

@DataJpaTest
@Import(QueryDslConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class BaseRepositoryTest {

	protected static Logger log = LoggerFactory.getLogger(BaseRepositoryTest.class);

	@PersistenceContext
	EntityManager entityManager;

	@AfterEach
	void tearDown() {
		flushAndClear();
	}

	protected void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
		log.debug("EntityManager flush and clear");
	}

	protected EntityManager getEntityManager() {
		return this.entityManager;
	}

	protected JPAQueryFactory getJPAQuery() {
		return new JPAQueryFactory(getEntityManager());
	}
}

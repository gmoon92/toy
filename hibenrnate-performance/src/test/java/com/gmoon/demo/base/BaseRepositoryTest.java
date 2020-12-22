package com.gmoon.demo.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class BaseRepositoryTest {

    protected static Logger log = LoggerFactory.getLogger(BaseRepositoryTest.class);

    @PersistenceContext
    EntityManager entityManager;

    @AfterEach
    void tearDown() {
        flushAndClear();
    }

    private void flushAndClear() {
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

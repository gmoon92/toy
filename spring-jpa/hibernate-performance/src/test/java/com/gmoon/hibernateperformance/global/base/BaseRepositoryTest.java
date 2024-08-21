package com.gmoon.hibernateperformance.global.base;

import com.gmoon.hibernateperformance.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

@Import(QueryDslConfig.class)
@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

	protected static Logger log = LoggerFactory.getLogger(BaseRepositoryTest.class);

	@PersistenceContext
	protected EntityManager entityManager;

	@AfterEach
	void tearDown() {
		flushAndClear();
	}

	protected void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
		log.debug("EntityManager flush and clear");
	}
}

package com.gmoon.springsecurityjwt.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import com.gmoon.springsecurityjwt.config.JpaConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@Import({JpaConfig.class, QuickPerfSqlConfig.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class BaseDataJpaTest {
	@PersistenceContext
	private EntityManager entityManager;

	public void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}

package com.gmoon.springeventlistener.global;

import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@Import(QuickPerfSqlConfig.class)
public abstract class SupportDataJpaTest {

	@Autowired
	private EntityManager em;

	public void flushAndClear() {
		em.flush();
		em.clear();
	}
}

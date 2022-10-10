package com.gmoon.springeventlistener.global;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
// @Import(QuickPerfSqlConfig.class)
@OverrideAutoConfiguration(enabled = true)
public abstract class SupportDataJpaTest {

	@Autowired
	private EntityManager em;

	public void flushAndClear() {
		em.flush();
		em.clear();
	}
}

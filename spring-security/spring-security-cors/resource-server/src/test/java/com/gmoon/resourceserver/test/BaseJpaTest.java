package com.gmoon.resourceserver.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.resourceserver.config.JpaConfig;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseJpaTest {
	@Autowired
	EntityManager em;

	protected void flushAndClear() {
		em.flush();
		em.clear();
	}
}

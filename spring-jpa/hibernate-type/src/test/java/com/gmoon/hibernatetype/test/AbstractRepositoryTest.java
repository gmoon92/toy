package com.gmoon.hibernatetype.test;

import com.gmoon.hibernatetype.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Enumerated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(JpaConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractRepositoryTest {

	@Enumerated
	@Autowired
	private EntityManager em;

	protected void flushAndClear() {
		em.flush();
		em.clear();
	}
}

package com.gmoon.springquartzcluster.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class BaseDataJpaTest {

	@Autowired
	protected EntityManager entityManager;

	public void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}

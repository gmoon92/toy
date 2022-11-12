package com.gmoon.springjpaspecs.global.domain;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpaspecs.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
public abstract class SupportDataJpaTest {

	@Autowired
	EntityManager em;

	public void flushAndClear() {
		em.flush();
		em.clear();
	}
}

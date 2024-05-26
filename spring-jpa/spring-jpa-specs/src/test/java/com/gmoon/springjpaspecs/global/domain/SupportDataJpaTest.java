package com.gmoon.springjpaspecs.global.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.springjpaspecs.global.config.JpaConfig;

import jakarta.persistence.EntityManager;

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

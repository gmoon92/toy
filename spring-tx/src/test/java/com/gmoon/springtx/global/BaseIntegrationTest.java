package com.gmoon.springtx.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public abstract class BaseIntegrationTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	protected MockMvc mockMvc;

	protected void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}

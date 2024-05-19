package com.gmoon.springtx.global;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

	@PersistenceContext
	private EntityManager entityManager;

	protected MockMvc mockMvc;

	@BeforeEach
	void setUp(@Autowired WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .alwaysDo(MockMvcResultHandlers.print())
			 .build();
	}

	protected void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}

package com.gmoon.resourceserver.test;

import com.gmoon.resourceserver.jwt.JwtUtils;
import com.gmoon.resourceserver.user.User;
import com.gmoon.resourceserver.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public abstract class BaseIntegrationTest {
	protected static String jwt;

	@Autowired
	protected MockMvc mockMvc;

	@BeforeAll
	static void beforeAll(@Autowired UserRepository repository, @Autowired JwtUtils jwtUtils) {
		User admin = repository.findByUsername("admin");

		jwt = jwtUtils.generate(admin);
	}
}

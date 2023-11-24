package com.gmoon.batchinsert.accesslogs.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class AccessLogRepositoryQueryTest {

	@Autowired
	private AccessLogRepositoryQuery accessLogRepositoryQuery;

	@Test
	void findAllByUsername() {
		assertThat(accessLogRepositoryQuery.findAllByUsername("admin"))
			.isNotEmpty();
	}
}

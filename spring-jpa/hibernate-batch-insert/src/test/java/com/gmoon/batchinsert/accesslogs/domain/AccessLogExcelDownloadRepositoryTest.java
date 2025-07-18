package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccessLogExcelDownloadRepositoryTest {

	private static List<AccessLog> accessLogs;

	@Autowired
	private AccessLogExcelDownloadRepository repository;

	@BeforeAll
	static void beforeAll(@Autowired AccessLogRepository accessLogRepository) {
		accessLogs = accessLogRepository.findAll();
	}

	@DisplayName("JPA 2 sec")
	@Test
	void saveAll() {
		List<AccessLogExcelDownload> registered = accessLogs.stream()
			 .map(AccessLogExcelDownload::create)
			 .toList();

		repository.saveAll(registered);
	}

	@DisplayName("JOOQ 117m")
	@Test
	void bulkSaveAllAtJooq() {
		repository.bulkSaveAllAtJooq(accessLogs);
	}

	@DisplayName("StatelessSession 51m")
	@Test
	void bulkSaveAllAtStatelessSession() {
		repository.bulkSaveAllAtStatelessSession(accessLogs);
	}

	@Disabled
	@DisplayName("JPA not supported insert query"
		 + "https://github.com/querydsl/querydsl/issues/3027"
		 + "https://github.com/querydsl/querydsl/issues/2663"
		 + "https://github.com/querydsl/querydsl/issues/2724")
	@Test
	void bulkSaveAllAtQueryDsl() {
		repository.bulkSaveAllAtQueryDsl(accessLogs);
	}

	@AfterEach
	void tearDown() {
		repository.flush();
	}
}

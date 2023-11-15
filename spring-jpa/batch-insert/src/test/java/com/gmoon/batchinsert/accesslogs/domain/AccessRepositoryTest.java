package com.gmoon.batchinsert.accesslogs.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.batchinsert.accesslogs.domain.vo.OperatingSystem;
import com.gmoon.batchinsert.global.JpaConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(JpaConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccessRepositoryTest {

	@Autowired
	private AccessRepository repository;

	@Test
	void saveAll() {
		repository.saveAll(
			Arrays.asList(
				new AccessLog("gmoontest", "220.0.0.0", OperatingSystem.MAC),
				new AccessLog("gmoontest", "220.0.0.0", OperatingSystem.WINDOW),
				new AccessLog("gmoontest", "220.0.0.0", OperatingSystem.CHROME)
			));

		assertThatCode(() -> repository.findAllByUsername("gmoontest"))
			.doesNotThrowAnyException();
	}

	@Test
	void findAllByAttemptDtBetween() {
		LocalDate from = LocalDate.of(2022, 1, 1);
		LocalDate to = LocalDate.now();

		repository.findAllByAttemptDtBetween(from.atStartOfDay(), to.atTime(LocalTime.MAX));
	}
}

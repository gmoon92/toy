package com.gmoon.batchinsert.accesslogs.domain;

import com.gmoon.batchinsert.accesslogs.domain.vo.OperatingSystem;
import com.gmoon.batchinsert.global.config.jooq.JooqConfig;
import com.gmoon.batchinsert.global.config.persistence.JpaConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
@Import({JpaConfig.class, JooqConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccessLogRepositoryTest {

	@Autowired
	private AccessLogRepository repository;

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
	void findAllByAttemptAtBetween() {
		LocalDateTime from = LocalDate.of(2022, 1, 1).atStartOfDay();
		LocalDateTime to = LocalDate.now().atTime(LocalTime.MAX);
		ZoneOffset utc = ZoneOffset.UTC;

		repository.findAllByAttemptAtBetween(from.toInstant(utc), to.toInstant(utc));
	}
}

package com.gmoon.timesorteduniqueidentifier.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SnowflakeIdGeneratorTest {

	@Test
	void generate() {
		long workerId = 31;
		long dataCenterId = 31;

		SnowflakeIdGenerator worker = new SnowflakeIdGenerator(workerId, dataCenterId);

		long id = worker.generate();
		for (int i = 0; i < 1_000; i++) {
			long nextId = worker.generate();
			assertThat(nextId).isGreaterThan(id);

			log.info("id: {}, nextId: {}", id, nextId);
			id = nextId;
		}
	}

	@DisplayName(
		 "2010년 11월 4일 01:42:54.657 UTC" +
			  "Snowflake ID 시스템이 최초로 도입된 시점을 나타내며, " +
			  "모든 Snowflake ID의 타임스탬프는 이 시간으로부터 얼마나 지났는지를 기준으로 계산됩니다.")
	@Test
	void twEpoch() {
		assertThat(LocalDateTime.of(
					   2010,
					   11,
					   4,
					   1,
					   42,
					   54,
					   657000000
				  ).toInstant(ZoneOffset.UTC)
				  .toEpochMilli()
		).isEqualTo(1288834974657L);
	}
}

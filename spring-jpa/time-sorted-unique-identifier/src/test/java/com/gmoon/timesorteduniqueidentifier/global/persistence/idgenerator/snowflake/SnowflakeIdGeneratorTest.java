package com.gmoon.timesorteduniqueidentifier.global.persistence.idgenerator.snowflake;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SnowflakeIdGeneratorTest {

	private static SnowflakeIdGenerator generator;

	@BeforeAll
	static void beforeAll() {
		int workerId = 31;
		int dataCenterId = 31;
		generator = new SnowflakeIdGenerator(workerId, dataCenterId);
	}

	@RepeatedTest(100)
	void generate() {
		long id = generator.generate();
		for (int i = 0; i < 1_000; i++) {
			long nextId = generator.generate();
			assertThat(nextId).isGreaterThan(id)
				 .isPositive();

			log.info("id: {}, nextId: {}, {}", id, nextId, Timestamp.toInstant(BitAllocation.TIMESTAMP.extract(nextId)));
			id = nextId;
		}
	}

	@DisplayName("동시성 검증")
	@ParameterizedTest
	@ValueSource(ints = {1_000, 10_000, 50_000, 100_000, 500_000})
	void concurrency(int userAgent) {
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Callable<Long>> callables = IntStream.range(0, userAgent)
			 .mapToObj(i -> (Callable<Long>) () -> generator.generate())
			 .toList();

		Awaitility
			 .await("동시 요청자 수: " + userAgent)
			 .untilAsserted(
				  () -> assertThat(executor.invokeAll(callables)
					   .stream()
					   .map(future -> {
						   try {
							   return future.get();
						   } catch (InterruptedException | ExecutionException e) {
							   throw new RuntimeException(e);
						   }
					   })
					   .distinct()
					   .count()
				  ).isEqualTo(userAgent)
			 );

		executor.shutdownNow();
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

	@Nested
	class ExtractTest {

		@Test
		void test() {
			long workerId = 31;
			long dataCenterId = 30;

			SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(workerId, dataCenterId);
			long snowflakeId = idGenerator.generate();
			log.info("snowflakeId: {}", snowflakeId);

			log.info("Sign bit    : {}", BitAllocation.SIGN_BIT.extract(snowflakeId));
			log.info("Timestamp   : {}", BitAllocation.TIMESTAMP.extract(snowflakeId));
			log.info("Timestamp   : {}", Timestamp.toInstant(BitAllocation.TIMESTAMP.extract(snowflakeId)));
			log.info("Data center : {}", BitAllocation.DATA_CENTER.extract(snowflakeId));
			log.info("Worker id   : {}", BitAllocation.WORKER_ID.extract(snowflakeId));
			log.info("Sequence    : {}", BitAllocation.SEQUENCE.extract(snowflakeId));

			assertThat(BitAllocation.SIGN_BIT.extract(snowflakeId)).isZero();
			assertThat(BitAllocation.TIMESTAMP.extract(snowflakeId)).isGreaterThanOrEqualTo(Instant.now().getEpochSecond());
			assertThat(BitAllocation.DATA_CENTER.extract(snowflakeId)).isEqualTo(30);
			assertThat(BitAllocation.WORKER_ID.extract(snowflakeId)).isEqualTo(31);
			assertThat(BitAllocation.SEQUENCE.extract(snowflakeId)).isZero();
		}
	}
}

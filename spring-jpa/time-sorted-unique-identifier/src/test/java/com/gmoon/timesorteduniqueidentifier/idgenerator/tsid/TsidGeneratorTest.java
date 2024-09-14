package com.gmoon.timesorteduniqueidentifier.idgenerator.tsid;

import com.github.f4b6a3.tsid.TsidFactory;
import com.gmoon.timesorteduniqueidentifier.idgenerator.base.BitUtils;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TsidGeneratorTest {

	private static TsidGenerator generator;

	@BeforeAll
	static void beforeAll() {
		generator = new TsidGenerator();
	}

	@RepeatedTest(100)
	void generate() {
		BigInteger tsid = generator.generate();
		long timestamp = BitUtils.extract(tsid, TsidGenerator.Bits.TIMESTAMP, TsidGenerator.Bits.RANDOMNESS);
		long random = BitUtils.extract(tsid, TsidGenerator.Bits.RANDOM, TsidGenerator.Bits.COUNT);
		long count = BitUtils.extract(tsid, TsidGenerator.Bits.COUNT, 0);
		log.info("TSID       : {}", tsid);
		log.info("timestamp  : {}", Instant.ofEpochMilli(timestamp));
		log.info("random     : {}", random);
		log.info("count      : {}", count);

		assertThat(tsid).isPositive();
	}

	@DisplayName("128bits 를 16 진수 문자열로 표현")
	@Test
	void generateHex() {
		BigInteger tsid = generator.generate();
		String hexString = tsid.toString(16);

		log.info("tsid     : {}", tsid);
		log.info("hexString: {}", hexString);

		assertThat(tsid.toString().length()).isEqualTo(37);
		assertThat(hexString.length()).isEqualTo(31);
	}

	@DisplayName("동시성 검증")
	@ParameterizedTest
	@ValueSource(ints = {1_000, 10_000, 50_000, 100_000, 500_000})
	void concurrency(int userAgent) {
		verify(userAgent, () -> generator.generate());
	}

	private void verify(int userAgent, Callable<Number> generateId) {
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Callable<Number>> callables = IntStream.range(0, userAgent)
			 .mapToObj(i -> generateId)
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

	/**
	 * <a href="https://github.com/f4b6a3/tsid-creator/tree/master">f4b6a3 tsid creator</a>
	 */
	@DisplayName("f4b6a3 tsid 라이브러리 동시성 검증")
	@Nested
	class TsidFactoryTest {

		@DisplayName("동시성 검증")
		@ParameterizedTest
		@ValueSource(ints = {1_000, 10_000, 50_000, 100_000, 500_000})
		void node256(int userAgent) {
			int nodeMaxBits = 2 ^ 8;
			TsidFactory factory = TsidFactory.newInstance256(nodeMaxBits);

			verify(userAgent, () -> factory.create().toLong());
		}

		@DisplayName("동시성 검증")
		@ParameterizedTest
		@ValueSource(ints = {1_000, 10_000, 50_000, 100_000, 500_000})
		void node1024(int userAgent) {
			int nodeMaxBits = 2 ^ 10;
			TsidFactory factory = TsidFactory.newInstance1024(nodeMaxBits);

			verify(userAgent, () -> factory.create().toLong());
		}

		@DisplayName("동시성 검증")
		@ParameterizedTest
		@ValueSource(ints = {1_000, 10_000, 50_000, 100_000, 500_000})
		void node4096(int userAgent) {
			int nodeMaxBits = 2 ^ 12;
			TsidFactory factory = TsidFactory.newInstance4096(nodeMaxBits);

			verify(userAgent, () -> factory.create().toLong());
		}
	}
}

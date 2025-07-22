package com.gmoon.springpoi.benchmark;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class BenchmarkTest {

	@Test
	void excelSAX() throws Exception {
		String outputFilepath = getOutputFilepath(ExcelSAXBenchmark.class);

		log.info("outputFilepath: {}", outputFilepath);
		new Runner(new OptionsBuilder()
			 .include(ExcelSAXBenchmark.class.getName() + ".*")
			 .output(outputFilepath)
			 .addProfiler(GCProfiler.class)
			 .addProfiler(HeapMemoryProfiler.class)
			 .shouldFailOnError(true)
			 .shouldDoGC(true)
			 .build())
			 .run();
	}

	private String getOutputFilepath(Class<?> clazz) {
		return String.format("%s/%s/%s",
			 System.getProperty("user.dir"),
			 "src/test/resources/benchmark",
			 String.format("benchmark-%s-%s.txt", clazz.getSimpleName(), Instant.now().toString())
		);
	}
}

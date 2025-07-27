package com.gmoon.springpoi.benchmark;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Disabled
class BenchmarkTest {

	@Test
	void excelSAX() throws Exception {
		String outputFilepath = getOutputFilepath(ExcelSAXBenchmark.class);

		new Runner(new OptionsBuilder()
			 .include(ExcelSAXBenchmark.class.getName() + ".*")
			 .output(outputFilepath)
			 .addProfiler(GCProfiler.class)
			 .addProfiler(HeapMemoryProfiler.class)
			 .shouldFailOnError(true)
			 .shouldDoGC(true)
			 .jvmArgsAppend("-Dserver.port=9000")
			 .jvmArgs(
				  "-Xmx1024m",
				  "-XX:+HeapDumpOnOutOfMemoryError",
				  "-XX:HeapDumpPath=./benchmark-sax-heapdump.hprof"
			 )
			 .build())
			 .run();
	}

	private String getOutputFilepath(Class<?> clazz) {
		String outputFilepath = String.format("%s/%s/%s",
			 System.getProperty("user.dir"),
			 "src/test/resources/benchmark",
			 String.format("%s-%s.txt",
				  clazz.getSimpleName(),
				  LocalDateTime.now()
			 )
		);

		log.info("\noutputFilepath: {}", outputFilepath);
		return outputFilepath;
	}
}

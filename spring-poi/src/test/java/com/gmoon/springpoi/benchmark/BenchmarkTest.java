package com.gmoon.springpoi.benchmark;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * @see <a href="https://medium.com/@python-javascript-php-html-css/managing-memory-accumulation-in-jmh-benchmarks-effectively-a6615a8f1007">Managing Memory Accumulation in JMH Benchmarks Effectively</a>
 * */
@Slf4j
@Disabled
class BenchmarkTest {

	@Test
	void excelSAX() throws Exception {
		Class<ExcelSAXBenchmark> benchmarkClass = ExcelSAXBenchmark.class;
		String xAxisParamName = "excelDataRowSize";

		BenchmarkRunner.run(benchmarkClass, xAxisParamName);
	}
}

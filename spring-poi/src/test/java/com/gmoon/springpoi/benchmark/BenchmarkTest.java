package com.gmoon.springpoi.benchmark;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

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

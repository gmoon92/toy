package com.gmoon.springpoi.benchmark.chart;

import org.openjdk.jmh.infra.BenchmarkParams;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class MetricKey {
	private final String benchmarkName;
	private final String mode;
	private final String metricName;
	private final String param;

	public MetricKey(
		 String benchmarkName,
		 BenchmarkParams params,
		 String metricName,
		 String paramName
	) {
		this.benchmarkName = benchmarkName;
		this.mode = params.getMode().shortLabel();
		this.metricName = metricName;
		this.param = params.getParam(paramName);
	}
}

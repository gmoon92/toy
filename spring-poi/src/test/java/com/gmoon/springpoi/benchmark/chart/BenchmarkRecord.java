package com.gmoon.springpoi.benchmark.chart;

import org.openjdk.jmh.results.Result;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = "benchmarkName")
@ToString
public class BenchmarkRecord {
	private final String benchmarkName;
	private final double cnt;
	private final double score;
	private final double error;
	private final String unit;

	public BenchmarkRecord(Result<?> result) {
		this.benchmarkName = result.getLabel();
		this.cnt = result.getSampleCount();
		this.score = result.getScore();
		this.error = result.getScoreError();
		this.unit = result.getScoreUnit();
	}
}

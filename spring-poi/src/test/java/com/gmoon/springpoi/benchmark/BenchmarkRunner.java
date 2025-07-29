package com.gmoon.springpoi.benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.gmoon.springpoi.benchmark.chart.BenchmarkRecord;
import com.gmoon.springpoi.benchmark.chart.MetricKey;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BenchmarkRunner {
	private static final char[] SUPER = {'\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074',
		 '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'};

	private final Class<?> benchmarkClass;
	private final String xAxisParamName;
	private final Path targetDir;
	private final Options options;

	private BenchmarkRunner(Class<?> benchmarkClass, String xAxisParamName) throws Exception {
		this.xAxisParamName = xAxisParamName;
		this.benchmarkClass = benchmarkClass;
		String className = benchmarkClass.getSimpleName().toLowerCase();
		this.targetDir = Files.createDirectories(
			 Path.of(
				  System.getProperty("user.dir"),
				  "src/test/resources/benchmark",
				  className
			 )
		);
		this.options = new OptionsBuilder()
			 .include(benchmarkClass.getName() + ".*")
			 .output(getOutputFilepath(String.format("%s-%s.txt",
				  benchmarkClass.getSimpleName(),
				  LocalDateTime.now()
			 )))
			 .addProfiler(GCProfiler.class)
			 .addProfiler(HeapMemoryProfiler.class)
			 .shouldFailOnError(true)
			 .shouldDoGC(true)
			 .jvmArgsAppend("-Dserver.port=9000")
			 .jvmArgs(
				  "-Xmx1024m",
				  "-XX:+HeapDumpOnOutOfMemoryError",
				  String.format("-XX:HeapDumpPath=%s/%s-heapdump.hprof", targetDir.toAbsolutePath(), className)
			 )
			 .build();
	}

	public static void run(Class<?> benchmarkClass, String xAxisParamName) throws Exception {
		BenchmarkRunner runner = new BenchmarkRunner(benchmarkClass, xAxisParamName);
		runner.run();
	}

	private void run() throws Exception {
		Collection<RunResult> results = new Runner(options).run();
		createChart(results);
	}

	private void createChart(Collection<RunResult> results) throws Exception {
		Map<MetricKey, BenchmarkRecord> benchmarkMap = getBenchmarkMap(results);

		Set<MetricKey> metricKeys = benchmarkMap.keySet();
		String[] params = metricKeys.stream()
			 .map(MetricKey::getParam)
			 .distinct()
			 .sorted(Comparator.comparingDouble(Double::parseDouble))
			 .toArray(String[]::new);

		String[] modes = metricKeys.stream()
			 .map(MetricKey::getMode)
			 .distinct()
			 .sorted()
			 .toArray(String[]::new);

		String[] metricNames = metricKeys.stream()
			 .map(MetricKey::getMetricName)
			 .distinct()
			 .sorted()
			 .toArray(String[]::new);

		String[] benchmarkNames = metricKeys.stream()
			 .map(MetricKey::getBenchmarkName)
			 .distinct()
			 .sorted()
			 .toArray(String[]::new);

		for (String mode : modes) {
			List<XYChart> charts = new ArrayList<>();
			for (String metricName : metricNames) {
				String yAxisTitle = mode + "." + metricName;
				XYChart chart = createSingleChart(yAxisTitle);

				for (String benchmarkName : benchmarkNames) {
					List<Double> yValue = new ArrayList<>();
					for (String param : params) {
						MetricKey key = MetricKey.builder()
							 .benchmarkName(benchmarkName)
							 .mode(mode)
							 .metricName(metricName)
							 .param(param)
							 .build();

						BenchmarkRecord record = benchmarkMap.get(key);
						yValue.add(record.getScore());
					}
					double[] yData = yValue.stream()
						 .mapToDouble(d -> d != null ? d : Double.NaN)
						 .toArray();

					double[] xData = Arrays.stream(params)
						 .mapToDouble(Double::parseDouble)
						 .toArray();
					XYSeries series = chart.addSeries(benchmarkName, xData, yData);
					series.setMarker(SeriesMarkers.DIAMOND);
				}
				charts.add(chart);
				BitmapEncoder.saveBitmap(
					 chart,
					 getOutputFilepath(
						  "chart",
						  String.format("%s-%s-%s", benchmarkClass.getSimpleName(), mode, metricName)
					 ),
					 BitmapEncoder.BitmapFormat.PNG
				);
			}

			int chartCount = charts.size();
			int chartCols = (int)Math.ceil(Math.sqrt(chartCount));
			int chartRows = (int)Math.ceil((double)chartCount / chartCols);
			int required = chartRows * chartCols;
			while (charts.size() < required) {
				XYChart empty = newXYChartBuilder().build();
				charts.add(empty);
			}

			BitmapEncoder.saveBitmap(
				 charts,
				 chartRows,
				 chartCols,
				 getOutputFilepath("chart", String.format("%s-%s", benchmarkClass.getSimpleName(), mode)),
				 BitmapEncoder.BitmapFormat.PNG
			);
		}
	}

	private XYChart createSingleChart(String yAxisTitle) {
		XYChart chart = newXYChartBuilder()
			 .title(yAxisTitle)
			 .xAxisTitle(xAxisParamName)
			 .yAxisTitle(yAxisTitle)
			 .build();
		chart.setCustomYAxisTickLabelsFormatter(this::prettyNumber);
		chart.setCustomXAxisTickLabelsFormatter(this::prettyNumber);
		XYStyler styler = chart.getStyler();
		styler.setLegendPosition(Styler.LegendPosition.InsideNE);
		styler.setXAxisLogarithmic(true);
		return chart;
	}

	private String prettyNumber(double value) {
		if (value == 0) {
			return "0";
		}

		double abs = Math.abs(value);
		if (abs >= 1e-3 && abs < 1e4) {
			if (abs < 1) {
				return String.format("%.4f", value);
			}
			if (abs < 10) {
				return String.format("%.3f", value);
			}
			if (abs < 100) {
				return String.format("%.2f", value);
			}
			if (abs < 1000) {
				return String.format("%.1f", value);
			}

			return String.format("%,.0f", value);
		}

		int exp = (int)(Math.floor(Math.log10(abs)));
		double m = value / Math.pow(10, exp);
		return String.format("%.2f×10%s", m, toSuperscript(exp));
	}

	private String toSuperscript(int n) {
		StringBuilder sb = new StringBuilder();
		if (n < 0) {
			sb.append('\u207B'); // superscript minus
			n = -n;
		}
		for (char c : Integer.toString(n).toCharArray()) {
			sb.append(SUPER[c - '0']);
		}
		return sb.toString();
	}

	private XYChartBuilder newXYChartBuilder() {
		return new XYChartBuilder()
			 .width(700)
			 .height(400);
	}

	private Map<MetricKey, BenchmarkRecord> getBenchmarkMap(Collection<RunResult> results) {
		Map<MetricKey, BenchmarkRecord> benchmarkMap = new HashMap<>();
		for (RunResult result : results) {
			BenchmarkParams params = result.getParams();

			Result<?> primaryResult = result.getPrimaryResult();
			String benchmarkName = primaryResult.getLabel();
			MetricKey metricKey = new MetricKey(
				 benchmarkName,
				 params,
				 "score",
				 xAxisParamName
			);
			benchmarkMap.put(metricKey, new BenchmarkRecord(primaryResult, params));

			Map<String, Result> secondaryResults = result.getSecondaryResults();
			for (Map.Entry<String, Result> entry : secondaryResults.entrySet()) {
				String metricName = entry.getKey();
				Result secondaryResult = entry.getValue();
				MetricKey secKey = new MetricKey(
					 benchmarkName,
					 params,
					 metricName,
					 xAxisParamName
				);

				benchmarkMap.put(secKey, new BenchmarkRecord(secondaryResult, params));
			}
		}

		return benchmarkMap;
	}

	private String getOutputFilepath(String first, String... more) {
		Path outputFilepath = targetDir.resolve(Path.of(first, more));
		log.info("\noutputFilepath: {}", outputFilepath);
		return outputFilepath.toAbsolutePath().toString();
	}
}

package com.gmoon.springpoi.chart;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import lombok.extern.slf4j.Slf4j;

/**
 * @see <a href="https://knowm.org/open-source/xchart/xchart-example-code/">knowm xchart</a>
 */
@Slf4j
@Disabled
class XChartTest {

	@Test
	void chart() throws IOException {
		double[] yData = new double[] {2.0, 1.0, 0.0};

		// Create Chart
		XYChart chart = new XYChart(500, 400);
		chart.setTitle("Sample Chart");
		chart.setXAxisTitle("X");
		chart.setXAxisTitle("Y");
		XYSeries series = chart.addSeries("y(x)", null, yData);
		series.setMarker(SeriesMarkers.CIRCLE);

		BitmapEncoder.saveBitmap(chart, getFilepath("Sample_Chart"), BitmapEncoder.BitmapFormat.PNG);
		BitmapEncoder.saveBitmap(chart, getFilepath("Sample_Chart"), BitmapEncoder.BitmapFormat.JPG);
		BitmapEncoder.saveBitmap(chart, getFilepath("Sample_Chart"), BitmapEncoder.BitmapFormat.BMP);
		BitmapEncoder.saveBitmap(chart, getFilepath("Sample_Chart"), BitmapEncoder.BitmapFormat.GIF);
		BitmapEncoder.saveJPGWithQuality(chart, getFilepath("Sample_Chart_With_Quality.jpg"), 0.95f);

		BitmapEncoder.saveBitmapWithDPI(chart, getFilepath("Sample_Chart_300_DPI"), BitmapEncoder.BitmapFormat.PNG,
			 300);
		BitmapEncoder.saveBitmapWithDPI(chart, getFilepath("Sample_Chart_300_DPI"), BitmapEncoder.BitmapFormat.JPG,
			 300);
		BitmapEncoder.saveBitmapWithDPI(chart, getFilepath("Sample_Chart_300_DPI"), BitmapEncoder.BitmapFormat.GIF,
			 300);

		VectorGraphicsEncoder.saveVectorGraphic(chart, getFilepath("Sample_Chart"),
			 VectorGraphicsEncoder.VectorGraphicsFormat.EPS);
		VectorGraphicsEncoder.saveVectorGraphic(chart, getFilepath("Sample_Chart"),
			 VectorGraphicsEncoder.VectorGraphicsFormat.PDF);
		VectorGraphicsEncoder.saveVectorGraphic(chart, getFilepath("Sample_Chart"),
			 VectorGraphicsEncoder.VectorGraphicsFormat.SVG);
	}

	private String getFilepath(String filename) {
		return Path.of(
			 System.getProperty("user.dir"),
			 "src/test/resources/xchart/benchmark",
			 filename
		).toAbsolutePath().toString();
	}
}

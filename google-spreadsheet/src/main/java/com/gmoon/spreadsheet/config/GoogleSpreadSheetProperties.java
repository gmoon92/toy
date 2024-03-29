package com.gmoon.spreadsheet.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "google.spreadsheet")
public class GoogleSpreadSheetProperties {

	private String id;

	private String range;

	private List<String> sheetNames;

	private List<String> sheetHeads;

	@Getter
	@Setter
	@ToString
	public static class DataSet {
		private String id;
	}

}

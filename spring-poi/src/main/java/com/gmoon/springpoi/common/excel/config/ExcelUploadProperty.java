package com.gmoon.springpoi.common.excel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "service.excel.upload")
@RequiredArgsConstructor
@Getter
public class ExcelUploadProperty {
	private final String storagePath;
	private final int maxRows;
	private final int chunkSize;
}

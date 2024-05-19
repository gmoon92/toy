package com.gmoon.batchinsert.accesslogs.api;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gmoon.batchinsert.accesslogs.application.AccessLogService;
import com.gmoon.batchinsert.global.prop.StorageProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/accesslog")
@RestController
@RequiredArgsConstructor
public class AccessLogController {

	private final AccessLogService accessLogService;
	private final StorageProperties storageProperties;
	private final ResourceLoader resourceLoader;

	@GetMapping("/download")
	public ResponseEntity<Resource> download(
		 @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate attemptDt) {
		log.info("accessLog {} download.", attemptDt);
		String filePath = getLocalStorageFilePath(attemptDt);
		File excelFile = accessLogService.createExcelFile(filePath, attemptDt);

		// Resource resource = resourceLoader.getResource("classpath:storage/" + excelFile.getName());
		Resource resource = new FileSystemResource(excelFile);
		return ResponseEntity.ok()
			 .contentType(MediaType.APPLICATION_OCTET_STREAM)
			 .cacheControl(CacheControl.noCache())
			 .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
				  .filename(excelFile.getName())
				  .build()
				  .toString())
			 .body(resource);
	}

	private String getLocalStorageFilePath(LocalDate attemptDt) {
		String fileName = "accesslog_" + attemptDt;
		return Paths.get(storageProperties.getAbsolutePath(), fileName)
			 .toString();
	}
}

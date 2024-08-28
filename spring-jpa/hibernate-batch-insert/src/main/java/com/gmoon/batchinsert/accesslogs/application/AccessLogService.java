package com.gmoon.batchinsert.accesslogs.application;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.batchinsert.accesslogs.domain.AccessLog;
import com.gmoon.batchinsert.accesslogs.domain.AccessLogExcelDownload;
import com.gmoon.batchinsert.accesslogs.domain.AccessLogExcelDownloadRepository;
import com.gmoon.batchinsert.accesslogs.domain.AccessLogRepository;
import com.gmoon.javacore.util.ExcelUtils;
import com.gmoon.javacore.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AccessLogService {

	private final AccessLogRepository accessLogRepository;
	private final AccessLogExcelDownloadRepository accessLogExcelDownloadRepository;

	@Transactional
	public File createExcelFile(String filePath, LocalDate attemptDt) {
		if (FileUtils.exists(filePath)) {
			return FileUtils.getResourceFile(filePath);
		}

		List<AccessLogExcelDownload> accessLogExcelDownloads = getAccessLogExcelDownloads(attemptDt);
		Path uploadPath = ExcelUtils.upload(filePath, accessLogExcelDownloads, AccessLogExcelDownload.class);
		log.info("upload file to local storage: {}", uploadPath);
		return uploadPath.toFile();
	}

	private List<AccessLogExcelDownload> getAccessLogExcelDownloads(LocalDate attemptDt) {
		LocalDateTime from = attemptDt.atStartOfDay();
		LocalDateTime to = attemptDt.plusYears(1).atTime(LocalTime.MAX);
		ZoneOffset utc = ZoneOffset.UTC;
		List<AccessLog> accessLogs = accessLogRepository.findAllByAttemptAtBetween(from.toInstant(utc),
			 to.toInstant(utc));
		return accessLogExcelDownloadRepository.bulkSaveAllAtJooq(accessLogs);
	}
}

package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccessLogExcelDownloadRepositoryQuery {

	List<AccessLogExcelDownload> bulkSaveAllAtStatelessSession(List<AccessLog> accessLogs);

	List<AccessLogExcelDownload> bulkSaveAllAtQueryDsl(List<AccessLog> accessLogs);
}

package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

public interface AccessLogExcelDownloadRepositoryQuery {

	List<AccessLogExcelDownload> bulkSaveAllAtStatelessSession(List<AccessLog> accessLogs);

	@Deprecated
	List<AccessLogExcelDownload> bulkSaveAllAtQueryDsl(List<AccessLog> accessLogs);
}

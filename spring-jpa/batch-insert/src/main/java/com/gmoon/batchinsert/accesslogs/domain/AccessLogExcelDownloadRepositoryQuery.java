package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

public interface AccessLogExcelDownloadRepositoryQuery {

	List<AccessLogExcelDownload> bulkSaveAllAtStatelessSession(List<AccessLog> accessLogs);
}

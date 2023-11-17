package com.gmoon.batchinsert.accesslogs.domain;

import java.util.List;

public interface AccessLogExcelDownloadRepositoryJooq {

	List<AccessLogExcelDownload> bulkSaveAllAtJooq(List<AccessLog> accessLogs);
}

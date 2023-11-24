package com.gmoon.batchinsert.accesslogs.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogExcelDownloadRepository extends JpaRepository<AccessLogExcelDownload, String>,
	AccessLogExcelDownloadRepositoryJooq,
	AccessLogExcelDownloadRepositoryQuery {
}

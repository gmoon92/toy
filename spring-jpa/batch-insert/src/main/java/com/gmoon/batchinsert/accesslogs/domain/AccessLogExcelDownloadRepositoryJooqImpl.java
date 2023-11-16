package com.gmoon.batchinsert.accesslogs.domain;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.InsertValuesStepN;

import com.gmoon.batchinsert.global.jmodel.tables.ExAccessLog;
import com.gmoon.batchinsert.global.jmodel.tables.records.ExAccessLogRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AccessLogExcelDownloadRepositoryJooqImpl implements AccessLogExcelDownloadRepositoryJooq {

	private static final ExAccessLog jExAccessLog = ExAccessLog.EX_ACCESS_LOG;
	private final DSLContext dsl;

	// https://www.jooq.org/doc/latest/manual/sql-building/sql-statements/insert-statement/insert-values/
	// Jooq 타입 추론 이슈 https://github.com/jOOQ/jOOQ/issues/6604
	@Override
	public List<AccessLogExcelDownload> bulkSaveAll(List<AccessLog> accessLogs) {
		InsertValuesStepN<ExAccessLogRecord> insertInto = (InsertValuesStepN<ExAccessLogRecord>)dsl.insertInto(
			jExAccessLog,
			jExAccessLog.ID,
			jExAccessLog.USERNAME,
			jExAccessLog.IP,
			jExAccessLog.OS,
			jExAccessLog.ATTEMPT_DT
		);

		List<AccessLogExcelDownload> result = new ArrayList<>(accessLogs.size());
		for (AccessLog accessLog : accessLogs) {
			AccessLogExcelDownload accessLogExcelDownload = AccessLogExcelDownload.create(accessLog);
			insertInto.values(
				accessLogExcelDownload.getId(),
				accessLogExcelDownload.getUsername(),
				accessLogExcelDownload.getIp(),
				accessLogExcelDownload.getOs(),
				accessLogExcelDownload.getAttemptDt()
			);

			result.add(accessLogExcelDownload);
		}

		insertInto.execute();

		return result;
	}
}

package com.gmoon.springpoi.excels.application;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gmoon.springpoi.excels.domain.ExcelUploadTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelPushService {

	@Async
	public void publishUploadTaskProgress(ExcelUploadTask task) {
		log.info("publish upload task progress: {}, total: {}, processed rows: {}, invalid rows: {}",
			 task.getStatus(),
			 task.getTotalRowCount(),
			 task.getProcessedRowCount(),
			 task.getInvalidRowCount()
		);
	}
}

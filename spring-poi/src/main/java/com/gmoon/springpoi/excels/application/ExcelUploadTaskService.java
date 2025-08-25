package com.gmoon.springpoi.excels.application;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springpoi.common.excel.config.ExcelUploadProperty;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
import com.gmoon.springpoi.excels.domain.ExcelUploadInvalidRow;
import com.gmoon.springpoi.excels.domain.ExcelUploadTask;
import com.gmoon.springpoi.excels.domain.ExcelUploadTaskChunk;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellErrorMessages;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;
import com.gmoon.springpoi.excels.infra.ExcelUploadInvalidRowRepository;
import com.gmoon.springpoi.excels.infra.ExcelUploadTaskChunkRepository;
import com.gmoon.springpoi.excels.infra.ExcelUploadTaskRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ExcelUploadTaskService {
	private final ExcelPushService pushService;

	private final ExcelUploadTaskRepository repository;
	private final ExcelUploadTaskChunkRepository chunkRepository;
	private final ExcelUploadInvalidRowRepository invalidRowRepository;

	private final ExcelUploadProperty property;

	@Transactional
	public ExcelUploadTask save(
		 ExcelSheetType sheetType,
		 long dataRowCount,
		 String filename,
		 Locale locale,
		 String timezone
	) {
		int chunkSize = property.getChunkSize();

		ExcelUploadTask saved = new ExcelUploadTask(sheetType, dataRowCount, chunkSize)
			 .withFilename(filename)
			 .withRequesterLocale(locale, timezone);
		return repository.save(saved);
	}

	@Transactional
	public ExcelUploadTask startProcessing(String taskId, String chunkId) {
		ExcelUploadTask task = repository.getWithLock(taskId);
		task.startProcessing();

		ExcelUploadTaskChunk chunk = chunkRepository.getReferenceById(chunkId);
		chunk.startProcessing();
		invalidRowRepository.removeAllByChunk(chunk);
		return task;
	}

	@Transactional
	public ExcelUploadTaskChunk completeChunk(String chunkId) {
		ExcelUploadTaskChunk chunk = chunkRepository.getReferenceById(chunkId);
		long chunkInvalidDataCount = invalidRowRepository.countByChunk(chunk);

		chunk.complete(chunkInvalidDataCount);
		return chunk;
	}

	@Transactional
	public void failChunk(String chunkId, String errorMsg) {
		ExcelUploadTaskChunk chunk = chunkRepository.getReferenceById(chunkId);
		chunk.fail(errorMsg);
	}

	@Transactional
	public ExcelUploadTask updateTaskSummary(String taskId) {
		ExcelUploadTask task = repository.getWithLock(taskId);

		task.mergeChunkResults();
		log.debug("update task summary: {}, processed rows: {} invalid rows: {}",
			 taskId,
			 task.getProcessedRowCount(),
			 task.getInvalidRowCount()
		);
		return task;
	}

	public ExcelUploadTaskChunk getChunk(String chunkId) {
		return chunkRepository.findById(chunkId)
			 .orElseThrow(EntityNotFoundException::new);
	}

	public ExcelUploadTask getTask(String taskId) {
		return repository.findById(taskId)
			 .orElseThrow(EntityNotFoundException::new);
	}

	public void saveInvalidRows(String chunkId, Set<ExcelInvalidRow> invalidRows) {
		ExcelUploadTaskChunk chunk = chunkRepository.getReferenceById(chunkId);
		for (ExcelInvalidRow row : invalidRows) {
			ExcelUploadInvalidRow invalidRow = new ExcelUploadInvalidRow(chunk);

			ExcelCellValues cellValues = row.getCellValues();
			ExcelCellErrorMessages errorMessages = row.getErrorMessages();

			invalidRow.setInvalidCellValues(cellValues, errorMessages);
			invalidRow.setInvalidType(row.getType());

			invalidRowRepository.save(invalidRow);
		}

		pushService.publishUploadTaskProgress(chunk.getTask());
	}
}

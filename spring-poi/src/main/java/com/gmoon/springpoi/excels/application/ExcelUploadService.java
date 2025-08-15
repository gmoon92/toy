package com.gmoon.springpoi.excels.application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springpoi.common.excel.config.ExcelUploadProperty;
import com.gmoon.springpoi.common.excel.exception.ExcelEmptyDataRowException;
import com.gmoon.springpoi.common.excel.exception.NotFoundExcelFileException;
import com.gmoon.springpoi.common.excel.helper.ExcelHelper;
import com.gmoon.springpoi.common.exception.InvalidFileException;
import com.gmoon.springpoi.excels.domain.ExcelSheetType;
import com.gmoon.springpoi.excels.domain.ExcelUploadJob;
import com.gmoon.springpoi.excels.infra.ExcelUploadJobRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ExcelUploadService {

	private final ExcelUploadJobRepository repository;

	private final ExcelHelper excelHelper;
	private final ExcelUploadProperty property;

	@Transactional
	public ExcelUploadJob upload(File file, ExcelSheetType sheetType) {
		validateFile(file);
		Path path = Paths.get(file.getAbsolutePath());

		long dataRows = getDataRows(sheetType, path);
		int chunkSize = property.getChunkSize();

		return repository.save(new ExcelUploadJob(sheetType, dataRows, chunkSize));
	}

	private long getDataRows(ExcelSheetType sheetType, Path path) {
		long dataRows = excelHelper.getDataRows(path, sheetType, property.getMaxRows());
		if (dataRows == 0) {
			throw new ExcelEmptyDataRowException();
		}
		return dataRows;
	}

	private void validateFile(File file) {
		if (file == null || !file.exists()) {
			throw new NotFoundExcelFileException(file);
		}

		if (!file.canRead()) {
			throw new InvalidFileException("File is not readable!");
		}

		String filename = file.getName().toLowerCase();
		if (!filename.endsWith(".xlsx")) {
			throw new InvalidFileException("Only .xlsx files are allowed: " + filename);
		}
	}
}

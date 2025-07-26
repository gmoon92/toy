package com.gmoon.springpoi.excel.processor;

import java.util.List;

import com.gmoon.springpoi.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.excel.vo.ExcelSheet;

@ExcelComponent
public class ExcelBatchPostListener implements EventListener {

	@Override
	public void onEvent(ExcelSheet<?> excelSheet) {
		ExcelModelMetadata metadata = excelSheet.getMetadata();
		List<ExcelBatchValidator> batchValidators = metadata.getAllBatchValidators();
		for (ExcelBatchValidator validator : batchValidators) {
			validator.flush(excelSheet::addInvalidRows);
		}
	}
}

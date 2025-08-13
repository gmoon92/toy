package com.gmoon.springpoi.common.excel.processor;

import java.util.List;

import com.gmoon.springpoi.common.excel.annotation.ExcelComponent;
import com.gmoon.springpoi.common.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;

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

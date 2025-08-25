package com.gmoon.springpoi.common.excel.sax.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.helpers.DefaultHandler;

import com.gmoon.springpoi.common.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.common.excel.validator.ExcelValidator;
import com.gmoon.springpoi.common.excel.vo.BaseExcelModel;
import com.gmoon.springpoi.common.excel.vo.ExcelCell;
import com.gmoon.springpoi.common.excel.vo.ExcelField;
import com.gmoon.springpoi.common.excel.vo.ExcelInvalidRow;
import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;
import com.gmoon.springpoi.excels.domain.vo.ExcelCellValues;

import lombok.extern.slf4j.Slf4j;

/**
 * SAX 이벤트 기반의 엑셀(xlsx) OOXML 파서
 *
 * <pre>
 * 예시 XML 구조:
 *   &lt;row r="1"&gt;
 *
 *     &lt;c r="A1" t="inlineStr"&gt;
 *        &lt;is&gt;
 *          &lt;t&gt;사용자 아이디&lt;/t&gt;
 *        &lt;/is&gt;
 *     &lt;/c&gt;
 *
 *     &lt;c r="B1" t="s"&gt;
 *       &lt;v&gt;0&lt;/v&gt;
 *     &lt;/c&gt;
 *
 *     &lt;c r="C1"&gt;
 *       &lt;v&gt;123&lt;/v&gt; // 숫자 또는 날짜
 *     &lt;/c&gt;
 *   &lt;/row&gt;
 * </pre>
 * {@link DefaultHandler} 기반으로 row/cell/value 등의 태그 이벤트를 처리하며,
 * ExcelSheet<T> 및 ExcelFields 와 연동해 DTO 조립에 활용할 수 있다.
 *
 * @author gmoon
 */
@Slf4j
public class SaxXlsxSheetHandler<T extends BaseExcelModel> extends AbstractSaxXlsxSheetHandler {
	private final ExcelSheet<T> excelSheet;
	private final RowCallbackHandler<T> rowCallbackHandler;

	public SaxXlsxSheetHandler(
		 SharedStrings sst,
		 ExcelSheet<T> excelSheet,
		 RowCallbackHandler<T> rowCallbackHandler,
		 long startRowIdx,
		 long endRowIdx
	) {
		super(sst, excelSheet.getMetadata(), startRowIdx, endRowIdx);
		this.excelSheet = excelSheet;
		this.rowCallbackHandler = rowCallbackHandler;
	}

	@Override
	public void handle(long dataRowIdx, Map<Integer, String> cellValues) {
		ExcelRow<T> excelRow = excelSheet.createRow(dataRowIdx);

		ExcelModelMetadata metadata = excelSheet.getMetadata();
		for (ExcelField excelField : metadata.getExcelFields()) {
			int cellColIdx = excelField.getCellColIndex();
			String cellValue = cellValues.get(cellColIdx);
			excelSheet.addOriginValue(dataRowIdx, cellColIdx, cellValue);

			List<ExcelValidator> failedValidators = excelField.getFailedValidators(cellValue);
			boolean validCellValue = failedValidators.isEmpty();
			if (validCellValue) {
				excelRow.setFieldValue(excelField, cellValue);

				List<ExcelBatchValidator> batchValidators = excelField.getBatchValidators();
				for (ExcelBatchValidator validator : batchValidators) {
					validator.collect(dataRowIdx, cellColIdx, cellValue);
					validator.flushBufferIfNeeded(excelSheet::addInvalidRows);
				}
			} else {
				excelSheet.addInvalidRow(dataRowIdx, new ExcelCell(cellColIdx, cellValue, failedValidators));
			}
		}

		if (isLastDataRow()) {
			flushBatchValidation(excelSheet);

			Map<Long, ExcelCellValues> originRows = excelSheet.getOriginRows();
			Set<ExcelRow<T>> rows = excelSheet.getRows();
			Set<ExcelInvalidRow> invalidRows = excelSheet.getInvalidRows();
			rowCallbackHandler.handle(originRows, rows, invalidRows);
		}
	}

	private void flushBatchValidation(ExcelSheet<T> excelSheet) {
		ExcelModelMetadata metadata = excelSheet.getMetadata();

		List<ExcelBatchValidator> allValidators = metadata.getAllBatchValidators();
		for (ExcelBatchValidator validator : allValidators) {
			validator.flush(excelSheet::addInvalidRows);
		}
	}
}

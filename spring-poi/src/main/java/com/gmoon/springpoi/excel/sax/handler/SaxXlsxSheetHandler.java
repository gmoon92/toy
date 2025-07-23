
package com.gmoon.springpoi.excel.sax.handler;

import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.helpers.DefaultHandler;

import com.gmoon.springpoi.excel.validator.ExcelBatchValidator;
import com.gmoon.springpoi.excel.vo.ExcelField;
import com.gmoon.springpoi.excel.vo.ExcelFields;
import com.gmoon.springpoi.excel.vo.ExcelRow;
import com.gmoon.springpoi.excel.vo.ExcelSheet;

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
 * <p>
 * 이벤트는 다음과 같은 순으로 동작된다.
 * <ul>
 *     <li>{@link SaxXlsxSheetHandler#startRow(int)}</li>
 *     <li>{@link SaxXlsxSheetHandler#handleCell(int, int, String)}</li>
 *     <li>{@link SaxXlsxSheetHandler#endRow(int)}</li>
 * </ul>
 * </p>
 *
 * @author gmoon
 */
@Slf4j
public class SaxXlsxSheetHandler<T> extends AbstractSaxXlsxSheetHandler {
	private static final int ACCESS_WINDOWS = 1_000;

	private final Class<T> excelModelClass;
	private final ExcelSheet<T> excelSheet;
	private final ExcelFields excelFields;
	private final Consumer<List<T>> rawCallback;

	public SaxXlsxSheetHandler(
		 SharedStrings sst,
		 Class<T> excelModelClass,
		 ExcelSheet<T> excelSheet,
		 ExcelFields excelFields,
		 Consumer<List<T>> rawCallback
	) {
		super(sst, excelFields.getTotalTitleRowCount());
		this.excelModelClass = excelModelClass;
		this.excelSheet = excelSheet;
		this.excelFields = excelFields;
		this.rawCallback = rawCallback;
	}

	@Override
	public void startRow(int rowIdx) {
		ExcelRow<T> excelRow = new ExcelRow<>(rowIdx, excelModelClass);
		excelSheet.add(rowIdx, excelRow);
	}

	@Override
	public void handleCell(int rowIdx, int cellColIdx, String cellValue) {
		ExcelField excelField = excelFields.getExcelField(cellColIdx);
		boolean invalidCellColIdx = excelField == null;
		if (invalidCellColIdx) {
			return;
		}

		ExcelRow<T> excelRow = excelSheet.getRowOrInvalidRow(rowIdx);
		excelRow.setFieldValue(excelField, cellValue);
		boolean invalidRow = !excelRow.isValid();
		if (invalidRow) {
			return;
		}

		boolean invalidCellValue = !excelField.isValidCellValue(cellValue);
		if (invalidCellValue) {
			excelSheet.addInvalidRow(rowIdx, excelRow);
		}
	}

	@Override
	public void endRow(int rowIdx) {
		if (isBlankRow()) {
			excelSheet.remove(rowIdx);
			return;
		}

		ExcelRow<T> excelRow = excelSheet.getRowOrInvalidRow(rowIdx);
		excelSheet.add(rowIdx, excelRow);
		List<T> rows = excelSheet.getRows();
		if (rows.size() > ACCESS_WINDOWS) {
			List<ExcelBatchValidator> allValidators = excelFields.getAllBatchValidators();
			for (ExcelBatchValidator validator : allValidators) {
				validator.flush(excelSheet::addInvalidRows);
			}

			rawCallback.accept(rows);
			excelSheet.clearRows();
		}
	}
}

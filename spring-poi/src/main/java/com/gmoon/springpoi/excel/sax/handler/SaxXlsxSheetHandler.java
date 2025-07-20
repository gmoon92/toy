
package com.gmoon.springpoi.excel.sax.handler;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.gmoon.springpoi.excel.processor.ExcelCellProcessor;
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
 *     <li>{@link SaxXlsxSheetHandler#startElement(String, String, String, Attributes)}</li>
 *     <li>{@link SaxXlsxSheetHandler#characters(char[], int, int)}</li>
 *     <li>{@link SaxXlsxSheetHandler#endElement(String, String, String)}</li>
 * </ul>
 * </p>
 *
 * @author gmoon
 */
@Slf4j
public class SaxXlsxSheetHandler<T> extends AbstractSaxXlsxSheetHandler {
	private final Class<T> excelModelClass;
	private final ExcelSheet<T> excelSheet;
	private final ExcelFields excelFields;
	private final ExcelCellProcessor<T> excelCellProcessor;

	public SaxXlsxSheetHandler(
		 SharedStrings sst,
		 Class<T> excelModelClass,
		 ExcelSheet<T> excelSheet,
		 ExcelFields excelFields,
		 ExcelCellProcessor<T> excelCellProcessor
	) {
		super(sst, excelFields.getTotalTitleRowCount());
		this.excelModelClass = excelModelClass;
		this.excelSheet = excelSheet;
		this.excelFields = excelFields;
		this.excelCellProcessor = excelCellProcessor;
	}

	@Override
	public void startRow(int rowIdx) {
		ExcelRow<T> excelRow = new ExcelRow<>(rowIdx, excelModelClass);
		excelSheet.add(rowIdx, excelRow);
	}

	@Override
	public void handleCell(int rowIdx, int cellColIdx, String cellValue) {
		ExcelField excelField = excelFields.getExcelField(cellColIdx);
		if (excelField == null) {
			return;
		}

		ExcelRow<T> excelRow = excelSheet.getExcelRow(rowIdx);
		if (excelRow == null) {
			return;
		}
		excelCellProcessor.process(excelSheet, excelRow, excelField, cellValue);
	}

	@Override
	public void endRow(int rowIdx) {
		if (isBlankRow()) {
			excelSheet.remove(rowIdx);
		} else {
			ExcelRow<T> excelRow = excelSheet.getExcelRow(rowIdx);
			excelSheet.add(rowIdx, excelRow);
		}
	}
}

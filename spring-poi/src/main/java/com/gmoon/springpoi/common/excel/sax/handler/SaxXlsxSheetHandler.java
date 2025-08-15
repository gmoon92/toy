
package com.gmoon.springpoi.common.excel.sax.handler;

import java.util.Map;

import org.apache.poi.xssf.model.SharedStrings;
import org.xml.sax.helpers.DefaultHandler;

import com.gmoon.springpoi.common.excel.processor.EventListener;
import com.gmoon.springpoi.common.excel.vo.ExcelCell;
import com.gmoon.springpoi.common.excel.vo.ExcelField;
import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;
import com.gmoon.springpoi.common.excel.vo.ExcelRow;
import com.gmoon.springpoi.common.excel.vo.ExcelSheet;

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
public class SaxXlsxSheetHandler<T> extends AbstractSaxXlsxSheetHandler {
	private static final int ACCESS_WINDOWS = 1_000;

	private final Class<T> excelModelClass;
	private final ExcelSheet<T> excelSheet;
	private final EventListener eventListener;

	public SaxXlsxSheetHandler(
		 SharedStrings sst,
		 Class<T> excelModelClass,
		 ExcelSheet<T> excelSheet,
		 EventListener eventListener,
		 int maxDataRows
	) {
		super(sst, excelSheet.getMetadata(), maxDataRows, 0, maxDataRows);
		this.excelModelClass = excelModelClass;
		this.excelSheet = excelSheet;
		this.eventListener = eventListener;
	}

	@Override
	public void handle(int rowIdx, Map<Integer, String> cellValues) {
		ExcelRow<T> excelRow = excelSheet.createRow(rowIdx, excelModelClass);

		ExcelModelMetadata metadata = excelSheet.getMetadata();
		for (Map.Entry<Integer, ExcelField> entry : metadata.entrySet()) {
			ExcelField excelField = entry.getValue();
			int cellColIdx = entry.getKey();
			String cellValue = cellValues.get(cellColIdx);

			if (excelField.isValidCellValue(cellValue)) {
				excelRow.setFieldValue(excelField, cellValue);
			} else {
				excelSheet.addInvalidRow(rowIdx, new ExcelCell(cellColIdx, cellValue));
			}
		}

		if (rowIdx % ACCESS_WINDOWS == 0) {
			eventListener.onEvent(excelSheet);
		}
	}
}

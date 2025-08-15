
package com.gmoon.springpoi.common.excel.sax.handler;

import java.util.Map;

import org.apache.poi.xssf.model.SharedStrings;

import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;

public class SaxDataRowHandler extends AbstractSaxXlsxSheetHandler {
	public SaxDataRowHandler(
		 SharedStrings sst,
		 ExcelModelMetadata metadata,
		 long maxDataRows
	) {
		super(sst, metadata, maxDataRows, 0, maxDataRows);
	}

	@Override
	public void handle(int rowIdx, Map<Integer, String> cellValues) {
		/* noop */
	}

	public long getDataRows() {
		return dataRows;
	}
}

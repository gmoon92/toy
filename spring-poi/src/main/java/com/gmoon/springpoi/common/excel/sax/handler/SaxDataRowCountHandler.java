package com.gmoon.springpoi.common.excel.sax.handler;

import java.util.Map;

import org.apache.poi.xssf.model.SharedStrings;

import com.gmoon.springpoi.common.excel.vo.ExcelModelMetadata;

public class SaxDataRowCountHandler extends AbstractSaxXlsxHandler {
	public SaxDataRowCountHandler(
		 SharedStrings sst,
		 ExcelModelMetadata metadata,
		 long maxDataRows
	) {
		super(sst, metadata, 0, maxDataRows);
	}

	@Override
	public void handle(long rowIdx, Map<Integer, String> cellValues) {
		/* noop */
	}

	public long getDataRows() {
		return dataRowIdx;
	}
}

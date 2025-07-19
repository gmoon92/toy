package com.gmoon.commons.commonsapachepoi.excel.sax;

import lombok.Getter;

@Getter
public class SaxCell {
	public static final SaxCell EMPTY = new SaxCell();

	private final XlsxOoxml.CellType type;
	private final int colIdx;
	private final String value;

	private SaxCell() {
		this.type = XlsxOoxml.CellType.UNKNOWN;
		this.colIdx = -1;
		this.value = null;
	}

	public SaxCell(String attributeValue, int colIdx) {
		this(XlsxOoxml.CellType.from(attributeValue), colIdx, null);
	}

	private SaxCell(XlsxOoxml.CellType type, int colIdx, String value) {
		this.type = type;
		this.colIdx = colIdx;
		this.value = value;
	}

	public SaxCell withValue(String value) {
		return new SaxCell(
			 type,
			 colIdx,
			 value
		);
	}
}

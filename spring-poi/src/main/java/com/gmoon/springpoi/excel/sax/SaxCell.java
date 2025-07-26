package com.gmoon.springpoi.excel.sax;

import lombok.Getter;

@Getter
public class SaxCell {
	public static final SaxCell EMPTY = new SaxCell();

	private final XlsxOoxml.CellType type;
	private final int columnIndex;
	private final String value;

	private SaxCell() {
		this.type = XlsxOoxml.CellType.UNKNOWN;
		this.columnIndex = -1;
		this.value = null;
	}

	public SaxCell(String attributeValue, int columnIndex) {
		this(XlsxOoxml.CellType.from(attributeValue), columnIndex, null);
	}

	private SaxCell(XlsxOoxml.CellType type, int columnIndex, String value) {
		this.type = type;
		this.columnIndex = columnIndex;
		this.value = value;
	}

	public SaxCell withValue(String value) {
		return new SaxCell(
			 type,
			 columnIndex,
			 value
		);
	}

	public boolean isEmpty() {
		return EMPTY == this;
	}
}

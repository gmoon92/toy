package com.gmoon.springpoi.common.excel.exception;

import java.io.Serial;

public class SaxReadRangeOverflowException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -2697133531192360187L;

	public SaxReadRangeOverflowException(long currentRow, long startRow, long endRow) {
		super(String.format(
			 "Section fully read: [%d ~ %d], stopped processing at row index %d. (This is an intentional flow-control signal.)",
			 startRow,
			 endRow,
			 currentRow
		));
	}
}

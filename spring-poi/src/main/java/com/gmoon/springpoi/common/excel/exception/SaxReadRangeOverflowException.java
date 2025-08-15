package com.gmoon.springpoi.common.excel.exception;

public class SaxReadRangeOverflowException extends RuntimeException {
	public SaxReadRangeOverflowException(long currentRow, long startRow, long endRow) {
		super(String.format(
			 "Section fully read: [%d ~ %d], stopped processing at row index %d. (This is an intentional flow-control signal.)",
			 startRow,
			 endRow,
			 currentRow
		));
	}
}

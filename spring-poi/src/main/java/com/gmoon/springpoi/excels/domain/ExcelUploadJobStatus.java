package com.gmoon.springpoi.excels.domain;

/**
 * <ul>
 *   <li>{@link #REQUEST_RECEIVED} 업로드 요청 수신</li>
 *   <li>{@link #PARSING} 엑셀 파싱 중</li>
 *   <li>{@link #SUCCESS} 업로드 전체 성공</li>
 *   <li>{@link #FAILED} 업로드 실패 또는 일부 작업만 성공(부분 성공 포함)</li>
 * </ul>
 */
public enum ExcelUploadJobStatus {
	REQUEST_RECEIVED,
	PARSING,
	SUCCESS,
	FAILED
}

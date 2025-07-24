package com.gmoon.springpoi.excel.domain;

/**
 * <ul>
 *   <li>{@link ExcelUploadStatus#REQUEST_RECEIVED} 업로드 요청 수신</li>
 *   <li>{@link ExcelUploadStatus#FILE_UPLOADED} 파일 업로드 완료</li>
 *   <li>{@link ExcelUploadStatus#PARSING} 엑셀 파싱 중</li>
 *   <li>{@link ExcelUploadStatus#SUCCESS} 업로드 처리 성공</li>
 *   <li>{@link ExcelUploadStatus#PARTIAL_SUCCESS} 처리 완료 일부 유효하지 않는 행 또는 DB 저장/수정/삭제중 에러 발생</li>
 *   <li>{@link ExcelUploadStatus#FAILED} 업로드 실패</li>
 * </ul>
 */
public enum ExcelUploadStatus {
	REQUEST_RECEIVED,
	FILE_UPLOADED,
	PARSING,
	SUCCESS,
	PARTIAL_SUCCESS,
	FAILED
}

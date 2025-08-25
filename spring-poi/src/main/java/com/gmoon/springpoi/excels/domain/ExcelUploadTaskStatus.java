package com.gmoon.springpoi.excels.domain;

/**
 * <ul>
 *   <li>{@link #PREPARING} 파싱 전 준비(행수 제한 검증, 파일 저장 등)</li>
 *   <li>{@link #PROCESSING} 엑셀 파싱 중</li>
 *   <li>{@link #SUCCESS} 업로드 처리 성공</li>
 *   <li>{@link #FAILED} 처리 완료 일부 유효하지 않는 행 또는 DB 저장/수정/삭제중 에러 발생</li>
 * </ul>
 */
public enum ExcelUploadTaskStatus {
	PREPARING,
	PROCESSING,
	SUCCESS,
	FAILED
}

package com.gmoon.springpoi.excel.domain;

/**
 * <ul>
 *     <li>{@link ExcelInvalidRowType#VALIDATION}: 입력값 검증 실패</li>
 *     <li>{@link ExcelInvalidRowType#PROCESS}: 비즈니스/DB 관련 오류</li>
 *     <li>{@link ExcelInvalidRowType#SYSTEM}: 시스템 에러</li>
 * </ul>
 * */
public enum ExcelInvalidRowType {
	VALIDATION, PROCESS, SYSTEM,
}

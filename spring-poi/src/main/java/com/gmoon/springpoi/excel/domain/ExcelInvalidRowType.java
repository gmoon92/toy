package com.gmoon.springpoi.excel.domain;

/**
 * <ul>
 *     <li>{@link #VALIDATION}: 입력값 검증 실패</li>
 *     <li>{@link #BUSINESS}: 서비스 메서드, 엔티티 메서드, 중복 등 비즈니스 로직 과정에서의 오류 및 데이터 무결성 등 포함</li>
 *     <li>{@link #SYSTEM}: 시스템 에러</li>
 * </ul>
 * */
public enum ExcelInvalidRowType {
	VALIDATION, BUSINESS, SYSTEM,
}

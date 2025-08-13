package com.gmoon.springpoi.excels.domain;

/**
 * 업로드 개별 작업(Task)의 상태.
 * <ul>
 *   <li>{@link #STARTING} : 작업 준비 중</li>
 *   <li>{@link #STARTED} : 업로드 작업 진행 중</li>
 *   <li>{@link #COMPLETED} : 모든 행이 정상적으로 처리되어 작업 종료</li>
 *   <li>{@link #PARTIAL_COMPLETED} : 모든 행을 처리하였으나 일부만 성공(유효하지 않은 행 존재 등)</li>
 *   <li>{@link #FAILED} : 작업 도중 예외 등으로 모든 행 처리 전 중단</li>
 * </ul>
 */
public enum ExcelUploadTaskStatus {
	STARTING,
	STARTED,
	COMPLETED,
	PARTIAL_COMPLETED,
	FAILED
}

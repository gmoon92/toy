package com.gmoon.cacheinvalidation.config;

/**
 * 애노테이션 경로에서 캐시 접근이 실패했다는 사실을 내보내는 쓰기 전용 포트다.
 * <p>
 * 코어는 집계하지 않는다. 기본 구현 {@link #NONE} 은 아무것도 하지 않으며,
 * 세는 구현은 측정이 필요한 쪽이 제공한다.
 */
@FunctionalInterface
public interface CacheFailureRecorder {

	CacheFailureRecorder NONE = operation -> {
	};

	void record(Operation operation);

	enum Operation {
		GET,
		PUT,
		EVICT,
		CLEAR
	}
}

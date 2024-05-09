package com.gmoon.javacore.test.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnionPageableTest {

	private int totalCount = 120;
	private int pageSize = 10;

	@Test
	void other00() {
		int prevTotalCount = 0;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPageSize()).isEqualTo(pageSize);
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(pageSize);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(pageSize * 2);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(pageSize * 3);
	}

	@Test
	void other09() {
		int prevTotalCount = 9;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPageSize()).isEqualTo(1);
		assertThat(page1.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(1);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(11);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(21);
	}

	@Test
	void other10() {
		int prevTotalCount = 10;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(10);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(20);
	}

	@Test
	void other11() {
		int prevTotalCount = 11;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(9);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(9);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(19);
	}

	private PaginatedVO getPaginatedVO(int requestPage, long prevTotalCount) {
		PaginatedVO vo = new PaginatedVO();
		vo.setTotalCount(totalCount);
		vo.setPageSize(pageSize);
		vo.setPage(requestPage);

		vo.resizingPage(prevTotalCount);
		log.debug("===========Page{}-{}===========", requestPage, prevTotalCount);
		log.debug("adjusted page           : {}", vo.getPage());
		log.debug("adjusted pageSize       : {}", vo.getPageSize());
		log.debug("adjusted offset         : {}", vo.getOffset());
		return vo;
	}
}

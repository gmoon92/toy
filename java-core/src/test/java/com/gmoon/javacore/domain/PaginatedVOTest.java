package com.gmoon.javacore.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PaginatedVOTest {

	private int totalCount = 120;
	private int pageSize = 10;

	@Test
	void prevTotalCount00() {
		int prevTotalCount = 0;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPage()).isEqualTo(0);
		assertThat(page1.getPageSize()).isEqualTo(pageSize);
		assertThat(page1.getOffset()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPage()).isEqualTo(1);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getOffset()).isEqualTo(pageSize);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPage()).isEqualTo(2);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getOffset()).isEqualTo(pageSize * 2);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPage()).isEqualTo(3);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getOffset()).isEqualTo(pageSize * 3);
	}

	@Test
	void prevTotalCount09() {
		int prevTotalCount = 9;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPage()).isEqualTo(0);
		assertThat(page1.getPageSize()).isEqualTo(1);
		assertThat(page1.getOffset()).isEqualTo(0);

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPage()).isEqualTo(1);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getOffset()).isEqualTo(1);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPage()).isEqualTo(2);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getOffset()).isEqualTo(11);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPage()).isEqualTo(3);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getOffset()).isEqualTo(21);
	}

	@Test
	void prevTotalCount10() {
		int prevTotalCount = 10;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPage()).isZero();
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getOffset()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPage()).isEqualTo(0);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getOffset()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPage()).isEqualTo(1);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getOffset()).isEqualTo(10);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPage()).isEqualTo(2);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getOffset()).isEqualTo(20);
	}

	@Test
	void prevTotalCount11() {
		int prevTotalCount = 11;

		PaginatedVO page1 = getPaginatedVO(1, prevTotalCount);
		assertThat(page1.getPage()).isZero();
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getOffset()).isZero();

		PaginatedVO page2 = getPaginatedVO(2, prevTotalCount);
		assertThat(page2.getPage()).isEqualTo(0);
		assertThat(page2.getPageSize()).isEqualTo(9);
		assertThat(page2.getOffset()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedVO(3, prevTotalCount);
		assertThat(page3.getPage()).isEqualTo(1);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getOffset()).isEqualTo(9);

		PaginatedVO page4 = getPaginatedVO(4, prevTotalCount);
		assertThat(page4.getPage()).isEqualTo(2);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getOffset()).isEqualTo(19);
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

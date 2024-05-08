package com.gmoon.javacore.test.dto;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
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

	private PaginatedVO getPaginatedVO(int page, long prevTotalCount) {
		PaginatedVO vo = new PaginatedVO();
		vo.setTotalCount(totalCount);
		vo.setPageSize(pageSize);
		vo.setPage(page);

		vo.resizingPage(prevTotalCount);
		return vo;
	}

	@Getter
	@Setter
	static class PaginatedVO {

		public static final int DEFAULT_PAGE_SIZE = 15;

		private long totalCount;
		private int pageSize;
		private long fullListSize;
		private String sortTargetColumn;
		private Integer page;
		private Integer firstRecordIndex;
		private Integer offset;

		public PaginatedVO() {
			this.pageSize = DEFAULT_PAGE_SIZE;
			this.page = 1;
			this.firstRecordIndex = 0;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
			this.firstRecordIndex = (this.page - 1) * this.pageSize;
		}

		public void setPage(Integer page) {
			if (page == null || page.compareTo(1) < 0)
				page = 1;
			this.page = page;
			this.firstRecordIndex = obtainOffset(this.page, this.pageSize);
		}

		protected int obtainOffset(int page, int pageSize) {
			return (page - 1) * pageSize;
		}

		public void resizingPage(long pageSize, long firstRecordIndex) {
			this.pageSize = Math.toIntExact(pageSize);
			this.firstRecordIndex = Math.toIntExact(firstRecordIndex);
		}

		public void resizingPage(long prevTotalCount) {
			final long requestPage = getPage();
			final long pageSize = getPageSize();

			final int dataPresent = 1;
			final long resizingStartPage = obtainTotalPage(prevTotalCount + dataPresent);
			final long prevLastPageSize = getLastPageSize(prevTotalCount);

			long adjustedPage = Math.max(requestPage - resizingStartPage, 0);
			long adjustedPageSize = 0;
			long adjustedOffset = adjustedPage * pageSize;

			if (requestPage == resizingStartPage) {
				adjustedPageSize = pageSize - prevLastPageSize;
			} else if (requestPage > resizingStartPage) {
				adjustedPageSize = pageSize;
				adjustedOffset -= prevLastPageSize;
			}

			resizingPage(adjustedPageSize, adjustedOffset);
			log.debug("===========Page{}-{}===========", requestPage, prevTotalCount);
			log.debug("resizingStartPage       : {}", resizingStartPage);
			log.debug("prevLastPageSize        : {}", prevLastPageSize);
			log.debug("adjusted page           : {}", adjustedPage);
			log.debug("adjusted pageSize       : {}", adjustedPageSize);
			log.debug("adjusted offset         : {}", adjustedOffset);
		}

		private long getLastPageSize(long totalCount) {
			int pageSize = getPageSize();
			int totalPage = obtainTotalPage(totalCount);
			long lastPageSize = pageSize - ((long)totalPage * pageSize - totalCount);
			if (pageSize == lastPageSize) {
				return 0;
			}
			return lastPageSize;
		}

		private int obtainTotalPage(long prevTotalCount) {
			long pageSize = getPageSize();
			return new BigDecimal(prevTotalCount)
				.divide(new BigDecimal(pageSize), RoundingMode.UP)
				.setScale(0, RoundingMode.DOWN)
				.toBigInteger()
				.intValue();
		}
	}
}

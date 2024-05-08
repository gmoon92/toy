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
	void otherEntity00() {
		int otherEntityTotalCount = 0;

		PaginatedVO page1 = getPaginatedListVO(1, otherEntityTotalCount);
		assertThat(page1.getPageSize()).isEqualTo(pageSize);
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedListVO(2, otherEntityTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(pageSize);

		PaginatedVO page3 = getPaginatedListVO(3, otherEntityTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(pageSize * 2);

		PaginatedVO page4 = getPaginatedListVO(4, otherEntityTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(pageSize * 3);
	}

	@Test
	void otherEntity09() {
		int otherEntityTotalCount = 9;

		PaginatedVO page1 = getPaginatedListVO(1, otherEntityTotalCount);
		assertThat(page1.getPageSize()).isEqualTo(1);
		assertThat(page1.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page2 = getPaginatedListVO(2, otherEntityTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(1);

		PaginatedVO page3 = getPaginatedListVO(3, otherEntityTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(11);

		PaginatedVO page4 = getPaginatedListVO(4, otherEntityTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(21);
	}

	@Test
	void otherEntity10() {
		int otherEntityTotalCount = 10;

		PaginatedVO page1 = getPaginatedListVO(1, otherEntityTotalCount);
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedListVO(2, otherEntityTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(pageSize);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedListVO(3, otherEntityTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(10);

		PaginatedVO page4 = getPaginatedListVO(4, otherEntityTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(20);
	}

	@Test
	void otherEntity11() {
		int otherEntityTotalCount = 11;

		PaginatedVO page1 = getPaginatedListVO(1, otherEntityTotalCount);
		assertThat(page1.getPageSize()).isZero();
		assertThat(page1.getFirstRecordIndex()).isZero();

		PaginatedVO page2 = getPaginatedListVO(2, otherEntityTotalCount);
		assertThat(page2.getPageSize()).isEqualTo(9);
		assertThat(page2.getFirstRecordIndex()).isEqualTo(0);

		PaginatedVO page3 = getPaginatedListVO(3, otherEntityTotalCount);
		assertThat(page3.getPageSize()).isEqualTo(pageSize);
		assertThat(page3.getFirstRecordIndex()).isEqualTo(9);

		PaginatedVO page4 = getPaginatedListVO(4, otherEntityTotalCount);
		assertThat(page4.getPageSize()).isEqualTo(pageSize);
		assertThat(page4.getFirstRecordIndex()).isEqualTo(19);
	}

	private PaginatedVO getPaginatedListVO(int page, long otherEntityTotalCount) {
		PaginatedVO listVO = new PaginatedVO();
		listVO.setTotalCount(totalCount);
		listVO.setPageSize(pageSize);
		listVO.setPage(page);

		listVO.resizingUserPage(otherEntityTotalCount);
		return listVO;
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
			return (page-1) * pageSize;
		}

		public void resizingPage(long pageSize, long firstRecordIndex) {
			this.pageSize = Math.toIntExact(pageSize);
			this.firstRecordIndex = Math.toIntExact(firstRecordIndex);
		}

		public void resizingUserPage(long otherEntityTotalCount) {
			final int otherEntityTotalPage = obtainOtherEntityTotalPage(otherEntityTotalCount);
			final int otherEntityLastPageSize = obtainOtherEntityLastPageSize(otherEntityTotalCount);
			final int otherEntitySize = obtainOtherEntitySize(otherEntityTotalPage, otherEntityLastPageSize);

			int pageSize = getPageSize();
			boolean otherEntityExists = otherEntityTotalCount > 0;
			boolean withOtherEntity = otherEntitySize < pageSize;
			boolean resizing = otherEntityExists && withOtherEntity;

			int userPage = getUserPage(otherEntityTotalPage, resizing);
			int userPageSize = pageSize - otherEntitySize;
			int userFirstRecordIndex = getUserFirstRecordIndex(userPage, userPageSize, otherEntityLastPageSize);

			resizingPage(userPageSize, userFirstRecordIndex);
			log.debug("===========page{}-{}===========", otherEntityTotalCount, getPage());
			log.debug("resizing         : {}", resizing);
			log.debug("includeOtherEntityPage : {}", otherEntitySize > 0);
			log.debug("otherEntityTotalPage   : {}", otherEntityTotalPage);
			log.debug("otherEntityLastPageSize: {}", otherEntityLastPageSize);
			log.debug("otherEntitySize        : {}", otherEntitySize);
			log.debug("userPage         : {}", userPage);
			log.debug("userPageSize     : {}", userPageSize);
			log.debug("userFirstRecord  : {}", userFirstRecordIndex);
		}

		private int getUserFirstRecordIndex(int userPage, int userPageSize, int otherEntityLastPageSize) {
			int offset = obtainOffset(userPage, userPageSize);
			int pushback = offset - otherEntityLastPageSize;
			return Math.max(pushback, 0);
		}

		private int obtainOtherEntityLastPageSize(long otherEntityTotalCount) {
			if (otherEntityTotalCount > 0) {
				int pageSize = getPageSize();
				int result = (int)(otherEntityTotalCount % pageSize);
				if (result == 0) {
					return pageSize;
				}

				return result;
			}

			return 0;
		}

		private int getUserPage(int otherEntityTotalPage, boolean resizing) {
			if (otherEntityTotalPage == 0) {
				return getPage();
			}

			int page = getPage();
			if (otherEntityTotalPage > page) {
				return 0;
			}

			int userPage = page - otherEntityTotalPage;
			if (resizing) {
				if (obtainOtherEntityLastPageSize(otherEntityTotalPage) < getPageSize()) {
					return ++userPage;
				}

				return userPage;
			}
			return userPage;
		}

		private int obtainOtherEntityTotalPage(long otherEntityTotalCount) {
			if (otherEntityTotalCount > 0) {
				int pageSize = getPageSize();
				return toBigDecimal(otherEntityTotalCount)
					.divide(toBigDecimal(pageSize), RoundingMode.UP)
					.setScale(0, RoundingMode.DOWN)
					.toBigInteger()
					.intValue();
			}

			return 0;
		}

		private int obtainOtherEntitySize(int otherEntityTotalPage, int otherEntityLastPageSize) {
			if (otherEntityTotalPage > 0) {
				int page = getPage();
				boolean lastPage = otherEntityTotalPage == page;
				if (lastPage) {
					return otherEntityLastPageSize;
				} else if (page < otherEntityTotalPage) {
					return getPageSize();
				}
			}
			return 0;
		}

		private BigDecimal toBigDecimal(long l) {
			return new BigDecimal(l);
		}
	}

}

package com.gmoon.javacore.domain;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class UnionPaginatedVOTest {

	private int pageSize = 10;

	@Test
	void team00() {
		int teamTotalCount = 0;
		BasePaginatedVO teamPaginatedVO = newTeamPaginatedVO(teamTotalCount);

		BasePaginatedVO page1 = getPaginatedVO(1, teamPaginatedVO);
		Assertions.assertThat(page1.getPage()).isEqualTo(1);
		Assertions.assertThat(page1.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page1.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page1.getOffset()).isEqualTo(0);

		BasePaginatedVO page2 = getPaginatedVO(2, teamPaginatedVO);
		Assertions.assertThat(page2.getPage()).isEqualTo(2);
		Assertions.assertThat(page2.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(1));
		Assertions.assertThat(page2.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page2.getOffset()).isEqualTo(pageSize);

		BasePaginatedVO page3 = getPaginatedVO(3, teamPaginatedVO);
		Assertions.assertThat(page3.getPage()).isEqualTo(3);
		Assertions.assertThat(page3.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(2));
		Assertions.assertThat(page3.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page3.getOffset()).isEqualTo(pageSize * 2);

		BasePaginatedVO page4 = getPaginatedVO(4, teamPaginatedVO);
		Assertions.assertThat(page4.getPage()).isEqualTo(4);
		Assertions.assertThat(page4.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(3));
		Assertions.assertThat(page4.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page4.getOffset()).isEqualTo(pageSize * 3);
	}

	@Test
	void team09() {
		int teamTotalCount = 9;
		BasePaginatedVO teamPaginatedVO = newTeamPaginatedVO(teamTotalCount);

		BasePaginatedVO page1 = getPaginatedVO(1, teamPaginatedVO);
		Assertions.assertThat(page1.getPage()).isEqualTo(1);
		Assertions.assertThat(page1.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page1.getPageSize()).isEqualTo(1);
		Assertions.assertThat(page1.getOffset()).isEqualTo(0);

		BasePaginatedVO page2 = getPaginatedVO(2, teamPaginatedVO);
		Assertions.assertThat(page2.getPage()).isEqualTo(2);
		Assertions.assertThat(page2.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(1));
		Assertions.assertThat(page2.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page2.getOffset()).isEqualTo(1);

		BasePaginatedVO page3 = getPaginatedVO(3, teamPaginatedVO);
		Assertions.assertThat(page3.getPage()).isEqualTo(3);
		Assertions.assertThat(page3.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(2));
		Assertions.assertThat(page3.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page3.getOffset()).isEqualTo(11);

		BasePaginatedVO page4 = getPaginatedVO(4, teamPaginatedVO);
		Assertions.assertThat(page4.getPage()).isEqualTo(4);
		Assertions.assertThat(page4.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(3));
		Assertions.assertThat(page4.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page4.getOffset()).isEqualTo(21);
	}

	@Test
	void team10() {
		int teamTotalCount = 10;
		BasePaginatedVO teamPaginatedVO = newTeamPaginatedVO(teamTotalCount);

		BasePaginatedVO page1 = getPaginatedVO(1, teamPaginatedVO);
		Assertions.assertThat(page1.getPage()).isEqualTo(1);
		Assertions.assertThat(page1.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page1.getPageSize()).isEqualTo(0);
		Assertions.assertThat(page1.getOffset()).isEqualTo(0);

		BasePaginatedVO page2 = getPaginatedVO(2, teamPaginatedVO);
		Assertions.assertThat(page2.getPage()).isEqualTo(1);
		Assertions.assertThat(page2.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page2.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page2.getOffset()).isEqualTo(0);

		BasePaginatedVO page3 = getPaginatedVO(3, teamPaginatedVO);
		Assertions.assertThat(page3.getPage()).isEqualTo(2);
		Assertions.assertThat(page3.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(1));
		Assertions.assertThat(page3.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page3.getOffset()).isEqualTo(10);

		BasePaginatedVO page4 = getPaginatedVO(4, teamPaginatedVO);
		Assertions.assertThat(page4.getPage()).isEqualTo(3);
		Assertions.assertThat(page4.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(2));
		Assertions.assertThat(page4.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page4.getOffset()).isEqualTo(20);
	}

	@Test
	void team11() {
		int teamTotalCount = 11;
		BasePaginatedVO teamPaginatedVO = newTeamPaginatedVO(teamTotalCount);

		BasePaginatedVO page1 = getPaginatedVO(1, teamPaginatedVO);
		Assertions.assertThat(page1.getPage()).isEqualTo(1);
		Assertions.assertThat(page1.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page1.getPageSize()).isEqualTo(0);
		Assertions.assertThat(page1.getOffset()).isEqualTo(0);

		BasePaginatedVO page2 = getPaginatedVO(2, teamPaginatedVO);
		Assertions.assertThat(page2.getPage()).isEqualTo(1);
		Assertions.assertThat(page2.getZeroBasedPage()).isEqualTo(ZeroBasedPage.zero());
		Assertions.assertThat(page2.getPageSize()).isEqualTo(9);
		Assertions.assertThat(page2.getOffset()).isEqualTo(0);

		BasePaginatedVO page3 = getPaginatedVO(3, teamPaginatedVO);
		Assertions.assertThat(page3.getPage()).isEqualTo(2);
		Assertions.assertThat(page3.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(1));
		Assertions.assertThat(page3.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page3.getOffset()).isEqualTo(9);

		BasePaginatedVO page4 = getPaginatedVO(4, teamPaginatedVO);
		Assertions.assertThat(page4.getPage()).isEqualTo(3);
		Assertions.assertThat(page4.getZeroBasedPage()).isEqualTo(ZeroBasedPage.from(2));
		Assertions.assertThat(page4.getPageSize()).isEqualTo(pageSize);
		Assertions.assertThat(page4.getOffset()).isEqualTo(19);
	}

	private BasePaginatedVO getPaginatedVO(int requestPage, BasePaginatedVO teamPaginatedVO) {
		BasePaginatedVO userPaginatedVO = new UserPaginatedVO();
		userPaginatedVO.setTotalCount(120);

		UnionPaginatedVO union = new UnionPaginatedVO(
			 requestPage,
			 pageSize,
			 teamPaginatedVO,
			 userPaginatedVO
		);

		log.debug("===========Page{}-{}===========", requestPage, teamPaginatedVO.getTotalCount());
		log.debug("adjusted page           : {}", userPaginatedVO.getPage());
		log.debug("adjusted zeroBasedPage  : {}", userPaginatedVO.getZeroBasedPage());
		log.debug("adjusted pageSize       : {}", userPaginatedVO.getPageSize());
		log.debug("adjusted offset         : {}", userPaginatedVO.getOffset());
		return union.get(UserPaginatedVO.class);
	}

	private BasePaginatedVO newTeamPaginatedVO(long totalCount) {
		BasePaginatedVO vo = new TeamPaginatedVO();
		vo.setTotalCount(totalCount);
		return vo;
	}

	@Test
	void instance() {
		BasePaginatedVO vo1 = new TeamPaginatedVO();
		vo1.setTotalCount(10);

		BasePaginatedVO vo2 = new UserPaginatedVO();
		vo2.setTotalCount(20);

		BasePaginatedVO vo3 = new UserPaginatedVO();
		vo3.setTotalCount(30);

		UnionPaginatedVO union = new UnionPaginatedVO(
			 1,
			 10,
			 vo1,
			 vo2,
			 vo3
		);

		assertThat(union.get(vo1).getTotalCount()).isEqualTo(10);
		assertThat(union.get(vo2).getTotalCount()).isEqualTo(20);
		assertThat(union.get(vo3).getTotalCount()).isEqualTo(30);
	}

	static class TeamPaginatedVO extends BasePaginatedVO {
	}

	static class UserPaginatedVO extends BasePaginatedVO {
	}
}

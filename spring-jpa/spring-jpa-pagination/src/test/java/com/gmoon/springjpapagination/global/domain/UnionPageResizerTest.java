package com.gmoon.springjpapagination.global.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class UnionPageResizerTest {

	@DisplayName("조직 그룹이 있는 경우 사용자 페이지 사이즈 조정")
	@Nested
	class ResizingUserPageTest {

		private int pageSize = 10;
		private int userTotalCount = 30;

		@Test
		void zero() {
			obtainProvider(0, 0, pageSize, 0);
			obtainProvider(1, 0, pageSize, 0);
			obtainProvider(0, 1, pageSize, 0);
		}

		@DisplayName("조직 그룹 데이터가 없을 경우")
		@Test
		void group00() {
			int groupTotalCount = 0;

			verifyUnmergedPage(resizedPage(1, groupTotalCount, UserGroupListVO.class));
			UserListVO page1 = resizedPage(1, groupTotalCount, UserListVO.class);
			verifyPage(page1, 1);
			verifyPageSize(page1, pageSize);
			verifyOffset(page1, 0);

			verifyUnmergedPage(resizedPage(2, groupTotalCount, UserGroupListVO.class));
			UserListVO page2 = resizedPage(2, groupTotalCount, UserListVO.class);
			verifyPage(page2, 2);
			verifyPageSize(page2, pageSize);
			verifyOffset(page2, pageSize);

			verifyUnmergedPage(resizedPage(3, groupTotalCount, UserGroupListVO.class));
			UserListVO page3 = resizedPage(3, groupTotalCount, UserListVO.class);
			verifyPage(page3, 3);
			verifyPageSize(page3, pageSize);
			verifyOffset(page3, pageSize * 2);

			verifyUnmergedPage(resizedPage(4, groupTotalCount, UserGroupListVO.class));
			verifyUnmergedPage(resizedPage(4, groupTotalCount, UserListVO.class));

			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserGroupListVO.class));
			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserListVO.class));
		}

		@DisplayName("pageSize=10, group=9, 첫페이지 사용자 데이터 1")
		@Test
		void group09() {
			int groupTotalCount = 9;

			UserGroupListVO groupPage1 = resizedPage(1, groupTotalCount, UserGroupListVO.class);
			verifyPage(groupPage1, 1);
			verifyPageSize(groupPage1, pageSize);
			verifyOffset(groupPage1, 0);

			UserListVO page1 = resizedPage(1, groupTotalCount, UserListVO.class);
			verifyPage(page1, 1);
			verifyPageSize(page1, 1);
			verifyOffset(page1, 0);

			verifyUnmergedPage(resizedPage(2, groupTotalCount, UserGroupListVO.class));
			UserListVO page2 = resizedPage(2, groupTotalCount, UserListVO.class);
			verifyPage(page2, 2);
			verifyPageSize(page2, pageSize);
			verifyOffset(page2, 1);

			verifyUnmergedPage(resizedPage(3, groupTotalCount, UserGroupListVO.class));
			UserListVO page3 = resizedPage(3, groupTotalCount, UserListVO.class);
			verifyPage(page3, 3);
			verifyPageSize(page3, pageSize);
			verifyOffset(page3, 11);

			verifyUnmergedPage(resizedPage(4, groupTotalCount, UserGroupListVO.class));
			UserListVO page4 = resizedPage(4, groupTotalCount, UserListVO.class);
			verifyPage(page4, 4);
			verifyPageSize(page4, pageSize);
			verifyOffset(page4, 21);

			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserGroupListVO.class));
			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserListVO.class));
		}

		@DisplayName("pageSize=10, group=10, 두번째 페이지부터 사용자 데이터 조회")
		@Test
		void group10() {
			int groupTotalCount = 10;

			UserGroupListVO groupPage1 = resizedPage(1, groupTotalCount, UserGroupListVO.class);
			verifyPage(groupPage1, 1);
			verifyPageSize(groupPage1, pageSize);
			verifyOffset(groupPage1, 0);
			verifyUnmergedPage(resizedPage(1, groupTotalCount, UserListVO.class));

			verifyUnmergedPage(resizedPage(2, groupTotalCount, UserGroupListVO.class));
			UserListVO page2 = resizedPage(2, groupTotalCount, UserListVO.class);
			verifyPage(page2, 1);
			verifyPageSize(page2, pageSize);
			verifyOffset(page2, 0);

			verifyUnmergedPage(resizedPage(3, groupTotalCount, UserGroupListVO.class));
			UserListVO page3 = resizedPage(3, groupTotalCount, UserListVO.class);
			verifyPage(page3, 2);
			verifyPageSize(page3, pageSize);
			verifyOffset(page3, 10);

			verifyUnmergedPage(resizedPage(4, groupTotalCount, UserGroupListVO.class));
			UserListVO page4 = resizedPage(4, groupTotalCount, UserListVO.class);
			verifyPage(page4, 3);
			verifyPageSize(page4, pageSize);
			verifyOffset(page4, 20);

			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserGroupListVO.class));
			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserListVO.class));
		}

		@DisplayName("pageSize=10, group=11, 두번째 페이지부터 사용자 데이터 조회")
		@Test
		void group11() {
			int groupTotalCount = 11;

			UserGroupListVO groupPage1 = resizedPage(1, groupTotalCount, UserGroupListVO.class);
			verifyPage(groupPage1, 1);
			verifyPageSize(groupPage1, pageSize);
			verifyOffset(groupPage1, 0);
			verifyUnmergedPage(resizedPage(1, groupTotalCount, UserListVO.class));

			UserGroupListVO groupPage2 = resizedPage(2, groupTotalCount, UserGroupListVO.class);
			verifyPage(groupPage2, 2);
			verifyPageSize(groupPage2, pageSize);
			verifyOffset(groupPage2, 10);
			UserListVO page2 = resizedPage(2, groupTotalCount, UserListVO.class);
			verifyPage(page2, 1);
			verifyPageSize(page2, 9);
			verifyOffset(page2, 0);

			verifyUnmergedPage(resizedPage(3, groupTotalCount, UserGroupListVO.class));
			UserListVO page3 = resizedPage(3, groupTotalCount, UserListVO.class);
			verifyPage(page3, 2);
			verifyPageSize(page3, pageSize);
			verifyOffset(page3, 9);

			verifyUnmergedPage(resizedPage(4, groupTotalCount, UserGroupListVO.class));
			UserListVO page4 = resizedPage(4, groupTotalCount, UserListVO.class);
			verifyPage(page4, 3);
			verifyPageSize(page4, pageSize);
			verifyOffset(page4, 19);

			verifyUnmergedPage(resizedPage(5, groupTotalCount, UserGroupListVO.class));
			UserListVO page5 = resizedPage(5, groupTotalCount, UserListVO.class);
			verifyPage(page5, 4);
			verifyPageSize(page5, pageSize);
			verifyOffset(page5, 29);
		}

		private void verifyUnmergedPage(Pageable pageable) {
			verifyPage(pageable, 1);
			verifyPageSize(pageable, 0);
			verifyOffset(pageable, 0);
		}

		private void verifyOffset(Pageable pageable, int expected) {
			assertThat(pageable.getOffset()).isEqualTo(expected);
		}

		private void verifyPage(Pageable pageable, int expected) {
			assertThat(pageable.getPage()).isEqualTo(expected);
		}

		private void verifyPageSize(Pageable pageable, int pageSize) {
			assertThat(pageable.getPageSize()).isEqualTo(pageSize);
		}

		private <T extends Pageable> T resizedPage(int page, int groupTotalCount, Class<T> clazz) {
			UnionPageResizer resizer = obtainProvider(page, pageSize, groupTotalCount, userTotalCount);
			T listVO = resizer.getPagination(clazz);
			log.debug("===========Page{}-{}===========", page, groupTotalCount);
			log.debug("{}", listVO.getClass().getSimpleName());
			log.debug("adjusted page           : {}", listVO.getPage());
			log.debug("adjusted pageSize       : {}", listVO.getPageSize());
			log.debug("adjusted offset         : {}", listVO.getOffset());
			return listVO;
		}
	}

	private UnionPageResizer obtainProvider(int page, int pageSize, long groupTotalCount, int userTotalCount) {
		return new UnionPageResizer(
			 page,
			 pageSize,
			 Arrays.asList(
				  userGroupListVO(groupTotalCount),
				  userListVO(userTotalCount)
			 )
		);
	}

	private UserGroupListVO userGroupListVO(long totalCount) {
		UserGroupListVO groupListVO = new UserGroupListVO();
		groupListVO.setTotalCount(totalCount);
		return groupListVO;
	}

	private UserListVO userListVO(int totalCount) {
		UserListVO userListVO = new UserListVO();
		userListVO.setTotalCount(totalCount);
		return userListVO;
	}

	static class UserGroupListVO extends Pageable {
	}

	static class UserListVO extends Pageable {
	}

}

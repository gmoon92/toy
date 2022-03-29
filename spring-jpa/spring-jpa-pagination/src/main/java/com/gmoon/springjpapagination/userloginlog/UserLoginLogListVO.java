package com.gmoon.springjpapagination.userloginlog;

import java.util.List;

import com.gmoon.springjpapagination.common.CursorPagination;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserLoginLogListVO extends CursorPagination {
	private String cursor;
	private boolean hasNextPage;

	private List<Data> list;

	@Getter
	@ToString
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Data {
		private String username;
		private AccessDevice accessDevice;
		private String attemptDateString;
		private boolean succeed;
		private String cursor;

		@QueryProjection
		public Data(String username, AccessDevice accessDevice, String attemptDateString, Boolean succeed, String cursor) {
			this.username = username;
			this.accessDevice = accessDevice;
			this.attemptDateString = attemptDateString;
			this.succeed = succeed;
			this.cursor = cursor;
		}
	}

	@Override
	public int getPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

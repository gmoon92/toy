package com.gmoon.springjpapagination.users.userloginlog.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.springjpapagination.global.domain.CursorPagination;
import com.gmoon.springjpapagination.users.userloginlog.domain.AccessDevice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserLoginLogListVO extends CursorPagination {
	private Cursor cursor;
	private boolean hasNextPage;

	private List<Data> list;

	@RequiredArgsConstructor
	@Getter
	@ToString
	public static class Cursor {
		private final String id;
		private final LocalDateTime attemptDt;
	}

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@ToString
	public static class Data {
		private String id;
		private String username;
		private AccessDevice accessDevice;
		private LocalDateTime attemptDt;
		private String attemptIp;
		private boolean succeed;

		@QueryProjection
		public Data(String id, String username, AccessDevice accessDevice, LocalDateTime attemptDt, String attemptIp, boolean succeed) {
			this.id = id;
			this.username = username;
			this.accessDevice = accessDevice;
			this.attemptDt = attemptDt;
			this.attemptIp = attemptIp;
			this.succeed = succeed;
		}
	}

	@Override
	public int getPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

package com.gmoon.springjpapagination.users.userloginlog.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.springjpapagination.global.domain.CursorPagination;
import com.gmoon.springjpapagination.users.userloginlog.domain.AccessDevice;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	public record Cursor(String id, Instant attemptAt) implements Serializable {
	}

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@ToString
	public static class Data {
		private String id;
		private String username;
		private AccessDevice accessDevice;
		private long attemptTime;
		private String attemptIp;
		private boolean succeed;

		@QueryProjection
		public Data(String id, String username, AccessDevice accessDevice, Instant attemptAt, String attemptIp,
			 boolean succeed) {
			this.id = id;
			this.username = username;
			this.accessDevice = accessDevice;
			this.attemptTime = attemptAt.toEpochMilli();
			this.attemptIp = attemptIp;
			this.succeed = succeed;
		}
	}

	@Override
	public int getPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}

package com.gmoon.springjpapagination.users.user.dto;

import java.io.Serializable;

import com.querydsl.core.annotations.QueryProjection;

import com.gmoon.springjpapagination.global.domain.BasePaginatedVO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserContentListVO extends BasePaginatedVO<UserContentListVO.Data> {

	private Search search = new Search();

	@NoArgsConstructor
	@Getter
	@Setter
	@ToString
	public static class Search implements Serializable {
		private KeywordType keywordType = KeywordType.USERNAME;
		private String keyword;
		private String groupId;

		public enum KeywordType {
			USERNAME
		}
	}

	@NoArgsConstructor
	@Getter
	@ToString
	public static class Data implements Serializable {

		private Type type;
		private String id;
		private String name;

		@QueryProjection
		public Data(Type type, String id, String name) {
			this.id = id;
			this.type = type;
			this.name = name;
		}

		public enum Type {
			USER_GROUP, USER
		}
	}
}

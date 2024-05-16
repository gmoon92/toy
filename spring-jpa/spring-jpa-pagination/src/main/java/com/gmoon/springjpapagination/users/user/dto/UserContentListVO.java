package com.gmoon.springjpapagination.users.user.dto;

import java.io.Serializable;
import java.util.List;

import com.gmoon.springjpapagination.global.domain.PaginatedContent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserContentListVO extends PaginatedContent<List<UserVO>> {

	private static final long serialVersionUID = -6252002883733589738L;

	private Search search = new Search();

	@NoArgsConstructor
	@Getter
	@Setter
	@ToString
	public static class Search implements Serializable {

		private static final long serialVersionUID = 1495636187673177941L;

		private String keyword;
		private String groupId;

	}
}

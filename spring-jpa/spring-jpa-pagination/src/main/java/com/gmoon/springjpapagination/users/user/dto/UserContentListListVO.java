package com.gmoon.springjpapagination.users.user.dto;

import java.io.Serializable;

import com.gmoon.springjpapagination.global.domain.BasePaginatedListVO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserContentListListVO extends BasePaginatedListVO<UserContentVO> {

	private Search search = new Search();

	@NoArgsConstructor
	@Getter
	@Setter
	@ToString
	public static class Search implements Serializable {
		private String keyword;
		private String groupId;

	}
}

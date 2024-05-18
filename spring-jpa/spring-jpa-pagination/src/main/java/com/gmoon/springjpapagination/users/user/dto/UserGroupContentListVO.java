package com.gmoon.springjpapagination.users.user.dto;

import java.util.List;

import com.gmoon.springjpapagination.global.domain.PaginatedContent;
import com.gmoon.springjpapagination.users.user.domain.UserGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserGroupContentListVO extends PaginatedContent<List<UserGroup>> {

	private static final long serialVersionUID = -115428938385684510L;
}

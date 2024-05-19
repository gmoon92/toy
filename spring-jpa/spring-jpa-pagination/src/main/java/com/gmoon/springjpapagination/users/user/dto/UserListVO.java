package com.gmoon.springjpapagination.users.user.dto;

import java.util.List;

import com.gmoon.springjpapagination.global.domain.PaginatedContent;
import com.gmoon.springjpapagination.users.user.domain.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserListVO extends PaginatedContent<List<User>> {

	private static final long serialVersionUID = 1847912838107715854L;
}

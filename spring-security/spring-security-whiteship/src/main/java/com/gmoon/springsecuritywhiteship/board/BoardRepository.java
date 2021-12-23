package com.gmoon.springsecuritywhiteship.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BoardRepository extends JpaRepository<Board, Long> {

	@Query("select b from Board b where b.author.id = ?#{principal.account.id}")
	List<Board> findCurrentUserBoards();
}

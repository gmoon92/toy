package com.gmoon.springsecurity.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

  @Query("select b from Board b where b.author.id = ?#{principal.account.id}")
  List<Board> findCurrentUserBoards();
}

package com.gmoon.quickperf.service;

import com.gmoon.quickperf.domain.Member;
import com.gmoon.quickperf.domain.Team;
import com.gmoon.quickperf.test.InitTestDataExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ExtendWith(QuickPerfTestExtension.class)
class TeamServiceTest extends InitTestDataExecutor {

  @Autowired
  private TeamService service;

  private Team backend;

  @BeforeEach
  void setUp() {
    backend = service.findByName("backend");
  }

  @Test
  @Transactional
  @ExpectUpdate
  @ExpectSelect(3)
  @DisplayName("기존의 팀의 맴버들을 새로운 팀으로 할당한다")
  void testMoveAllMemberToAssignedNewTeam() {
    // given
    Team newbie = service.findByName("newbie");
    List<Member> newbies = newbie.getMembers();

    // when
    service.moveAllMemberToAssignedNewTeam(newbies, backend.getId());

    // then
    flushAndClear();
  }
}
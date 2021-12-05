package com.gmoon.quickperf.repository;

import com.gmoon.quickperf.domain.Member;
import com.gmoon.quickperf.domain.MemberOption;
import com.gmoon.quickperf.domain.Team;
import com.gmoon.quickperf.test.InitTestDataExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ExtendWith(QuickPerfTestExtension.class)
@Import(QuickPerfSqlConfig.class)
class TeamRepositoryTest extends InitTestDataExecutor {
  @Autowired
  TeamRepository repository;

  @Test
  @ExpectSelect(8)
  @DisplayName("Team을 기준으로 모든 팀원(Member)을 조회할 시 " +
          "N+1 문제로 8 번의 쿼리가 발생한다." +
          "  Team         조회 1 " +
          "+ Company      조회 1 " +
          "+ newbie Member         조회 1 " +
          "+ newbie MemberOption   조회 1 " +
          "+ backend Member        조회 1 " +
          "+ backend MemberOption  조회 3 ")
  void testFindAll_N_plush_One() {
    // given
    List<Team> teams = repository.findAll();

    // when then
    for (Team team : teams) {
      for (Member member : team.getMembers()) {
        MemberOption memberOption = member.getMemberOption();
        assertThat(memberOption.isEnabled()).isTrue();
      }
    }
  }

  @Test
  @ExpectSelect
  @DisplayName("모든 팀원을 조회 한다. 팀원을 주체로 fetchJoin")
  void testFindAll_from_member_fetchJoin() {
    // given
    EntityManager em = getEntityManager();

    // when
    List<Member> members = em.createQuery("select m from Member m " +
                    "join fetch m.team t " +
                    "join fetch t.company c " +
                    "join fetch m.memberOption mo " +
                    "where m.team.id = :teamId")
            .setParameter("teamId", 2L)
            .getResultList();

    // then
    for (Member member : members) {
      MemberOption memberOption = member.getMemberOption();
      assertThat(memberOption.isEnabled()).isTrue();
    }
  }

  @Test
  @ExpectSelect
  @DisplayName("모든 팀원을 조회 한다. 팀을 주체로 컬렉션 fetch join ")
  void testFindAll_from_team_fetchJoin() {
    // given
    EntityManager em = getEntityManager();

    // when
    List<Team> teams = em.createQuery("select t from Team t " +
                    "join fetch t.company c " +
                    "join fetch t.members m " +
                    "join fetch m.memberOption mo ")
            .getResultList();

    // then
    for (Team team : teams) {
      for (Member member : team.getMembers()) {
        MemberOption memberOption = member.getMemberOption();
        assertThat(memberOption.isEnabled()).isTrue();
      }
    }
  }
}
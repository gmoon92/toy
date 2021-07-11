package com.gmoon.demo.team;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.member.Member;
import com.gmoon.demo.member.repository.MemberRepository;
import com.gmoon.demo.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
class TeamRepositoryTest extends BaseRepositoryTest {

  final TeamRepository teamRepository;

  @BeforeAll
  static void setup(@Autowired MemberRepository memberRepository, @Autowired TeamRepository teamRepository) {
    log.debug("Database data setup start...");
    memberRepository.deleteAllInBatch();
    teamRepository.deleteAllInBatch();

    Team web1 = teamRepository.save(Team.newInstance("web1"));
    memberRepository.saveAll(Arrays.asList(
            Member.newInstance("gmoon", web1)
            , Member.newInstance("kwon", web1)
            , Member.newInstance("kim", web1)
            , Member.newInstance("lee", web1)
    ));

    log.debug("Database data setup done...");
  }

  @Test
  void testFindAll() {
    List<Team> teamList = teamRepository.findAll();
//        Hibernate.isInitialized();

    teamList.forEach(team -> {
      log.debug("team : {}", team);
//            team.getMembers().forEach(member -> log.debug("member : {}, {}, {}", member.getName()
//                    , Hibernate.isInitialized(member.getMemberOption())
//                    , member.getMemberOption().isEnabled()));
    });

  }
}
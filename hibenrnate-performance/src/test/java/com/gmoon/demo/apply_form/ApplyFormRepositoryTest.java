package com.gmoon.demo.apply_form;

import com.gmoon.demo.base.BaseRepositoryTest;
import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.repository.MemberRepository;
import com.gmoon.demo.team.Team;
import com.gmoon.demo.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class ApplyFormRepositoryTest extends BaseRepositoryTest {

  final ApplyFormRepository applyFormRepository;
  final MemberRepository memberRepository;
  final TeamRepository teamRepository;

  Member member;
  Team team;

  @Test
  @DisplayName("저장 테스트")
  void save() {
    // given
    team = teamRepository.save(Team.newInstance("web1"));
    member = memberRepository.save(Member.newInstance("gmoon", team));

    // when
    ApplyForm newApplyForm = ApplyForm.newInstance(member, team);
    newApplyForm.fillOut("title", "content");
    ApplyForm applyForm = applyFormRepository.save(newApplyForm);

    // then
    assertThat(applyForm)
            .hasNoNullFieldsOrPropertiesExcept("id")
            .hasFieldOrPropertyWithValue("title", "title")
            .hasFieldOrPropertyWithValue("content", "content");
  }

  @Test
  @Transactional
  @DisplayName("업데이트 테스트")
  void update() {
    // given
    saveDummyData();

    // when
    ApplyForm applyForm = applyFormRepository.findByMemberAndTeam(member, team);

    // then
    applyForm.fillOut("title1", "content");
    flushAndClear();
  }

  private void saveDummyData() {
    team = teamRepository.save(Team.newInstance("web1"));
    member = memberRepository.save(Member.newInstance("gmoon", team));

    ApplyForm newApplyForm = ApplyForm.newInstance(member, team);
    newApplyForm.fillOut("title", "content");
    applyFormRepository.save(newApplyForm);
    flushAndClear();
  }
}
package com.gmoon.hibernateperformance.applyform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.gmoon.hibernateperformance.applyform.domain.ApplyForm;
import com.gmoon.hibernateperformance.applyform.domain.ApplyFormRepository;
import com.gmoon.hibernateperformance.global.base.BaseRepositoryTest;
import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.member.domain.MemberRepository;
import com.gmoon.hibernateperformance.team.domain.Team;
import com.gmoon.hibernateperformance.team.domain.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class ApplyFormRepositoryTest extends BaseRepositoryTest {

	private final ApplyFormRepository applyFormRepository;
	private final MemberRepository memberRepository;
	private final TeamRepository teamRepository;

	private Member member;
	private Team team;

	@BeforeEach
	void setUp() {
		team = teamRepository.saveAndFlush(Team.newInstance("web1"));
		member = memberRepository.saveAndFlush(Member.newInstance("gmoon"));

		ApplyForm newApplyForm = ApplyForm.newInstance(member, team);
		newApplyForm.fillOut("title", "content");

		ApplyForm applyForm = applyFormRepository.saveAndFlush(newApplyForm);
		assertThat(applyForm)
			 .hasNoNullFieldsOrPropertiesExcept("id")
			 .hasFieldOrPropertyWithValue("title", "title")
			 .hasFieldOrPropertyWithValue("content", "content");

		flushAndClear();
	}

	@DisplayName("업데이트 테스트")
	@Test
	void updateApplyForm() {
		ApplyForm applyForm = applyFormRepository.findByMemberAndTeam(member, team);

		applyForm.fillOut("title1", "content");

		assertThatCode(this::flushAndClear).doesNotThrowAnyException();
	}

	@DisplayName("양방향 설정, CaseCade remove delete query 발생하는지")
	@Test
	void deleteMember() {
		memberRepository.delete(member);

		assertThatCode(this::flushAndClear).doesNotThrowAnyException();
	}

	@DisplayName("양방향 설정, CaseCade remove delete query 발생하는지")
	@Test
	void deleteTeam() {
		teamRepository.delete(team);

		assertThatCode(this::flushAndClear).doesNotThrowAnyException();
	}
}

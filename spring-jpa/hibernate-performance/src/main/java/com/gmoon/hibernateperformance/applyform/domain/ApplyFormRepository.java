package com.gmoon.hibernateperformance.applyform.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.team.domain.Team;

public interface ApplyFormRepository extends JpaRepository<ApplyForm, ApplyForm.Id> {

	ApplyForm findByMemberAndTeam(Member member, Team team);
}

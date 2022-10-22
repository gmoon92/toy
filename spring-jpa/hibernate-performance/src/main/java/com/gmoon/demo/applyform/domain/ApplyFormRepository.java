package com.gmoon.demo.applyform.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.domain.Team;

public interface ApplyFormRepository extends JpaRepository<ApplyForm, Long> {

	ApplyForm findByMemberAndTeam(Member member, Team team);
}

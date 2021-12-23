package com.gmoon.demo.apply_form;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.Team;

public interface ApplyFormRepository extends JpaRepository<ApplyForm, Long> {

	ApplyForm findByMemberAndTeam(Member member, Team team);
}

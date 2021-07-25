package com.gmoon.demo.apply_form;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplyFormRepository extends JpaRepository<ApplyForm, Long> {

  ApplyForm findByMemberAndTeam(Member member, Team team);
}

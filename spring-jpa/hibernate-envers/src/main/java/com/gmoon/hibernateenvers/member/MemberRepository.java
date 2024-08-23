package com.gmoon.hibernateenvers.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gmoon.hibernateenvers.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

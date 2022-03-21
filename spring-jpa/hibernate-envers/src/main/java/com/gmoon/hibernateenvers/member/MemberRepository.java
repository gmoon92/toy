package com.gmoon.hibernateenvers.member;

import org.springframework.data.repository.CrudRepository;

import com.gmoon.hibernateenvers.member.domain.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {
}

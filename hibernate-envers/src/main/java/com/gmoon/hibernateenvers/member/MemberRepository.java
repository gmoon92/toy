package com.gmoon.hibernateenvers.member;

import com.gmoon.hibernateenvers.member.domain.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepository extends CrudRepository<Member, Long> {
}

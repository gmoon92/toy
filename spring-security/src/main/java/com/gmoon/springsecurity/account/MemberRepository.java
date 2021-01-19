package com.gmoon.springsecurity.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Member findByUsername(String username);
}

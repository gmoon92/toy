package com.gmoon.hibernatesecondlevelcache.member;

import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

  public Member getId(Long id) throws InterruptedException {
    Thread.sleep(2000);

    Member member = new Member();
    member.setId(id);
    member.setName("admin");
    return member;
  }
}

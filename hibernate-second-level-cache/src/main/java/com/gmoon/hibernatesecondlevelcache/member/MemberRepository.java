package com.gmoon.hibernatesecondlevelcache.member;

import com.gmoon.hibernatesecondlevelcache.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

  @Cacheable(value = CacheConfig.MEMBER_FIND_BY_ID, key = "#id")
  public Member getId(Long id) throws InterruptedException {
    Thread.sleep(2000);

    Member member = new Member();
    member.setId(id);
    member.setName("admin");
    return member;
  }
}

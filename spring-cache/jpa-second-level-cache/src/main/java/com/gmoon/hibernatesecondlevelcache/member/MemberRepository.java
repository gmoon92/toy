package com.gmoon.hibernatesecondlevelcache.member;

import java.util.Arrays;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.gmoon.hibernatesecondlevelcache.config.CachePolicy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class MemberRepository {

	@Cacheable(value = CachePolicy.CacheName.MEMBER_FIND_BY_ID, key = "#id")
	public Member getId(Long id) throws InterruptedException {
		log.info("getId start...");
		Thread.sleep(2000);

		Member member = new Member();
		member.setId(id);
		member.setName("admin");
		return member;
	}

	@Cacheable(value = CachePolicy.CacheName.MEMBER_ALL)
	public List<Member> getAll() throws InterruptedException {
		log.info("getAll start...");
		Thread.sleep(3000);

		Member adminUser = new Member();
		adminUser.setId(0L);
		adminUser.setName("admin");

		Member memberUser = new Member();
		memberUser.setId(1L);
		memberUser.setName("admin");
		return Arrays.asList(adminUser, memberUser);
	}
}

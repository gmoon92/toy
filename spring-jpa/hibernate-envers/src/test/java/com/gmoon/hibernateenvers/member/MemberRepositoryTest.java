package com.gmoon.hibernateenvers.member;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.hibernateenvers.member.domain.Member;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@BeforeAll
	static void beforeAll(@Autowired MemberRepository memberRepository) {
		Member member = Member.createNew("gmoon", "password");
		memberRepository.save(member);
	}

	@Test
	void revisionDataTest() {

	}
}

package com.gmoon.hibernateenvers.member;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.hibernateenvers.member.domain.Member;

@SpringBootTest
class MemberRepositoryTest {

	@BeforeAll
	static void beforeAll(@Autowired MemberRepository memberRepository) {
		Member member = Member.createNew("gmoon", "password");
		memberRepository.saveAndFlush(member);
	}

	@Test
	void test() {

	}
}

package com.gmoon.hibernateenvers.global.envers.listener;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.hibernateenvers.member.domain.Member;
import com.gmoon.hibernateenvers.member.infra.MemberRepository;
import com.gmoon.hibernateenvers.revision.domain.RevisionHistoryRepository;

@Transactional
@SpringBootTest
class RevisionHistoryEventListenerTest {

	@Autowired
	private RevisionHistoryRepository repository;

	@BeforeAll
	static void beforeAll(@Autowired MemberRepository memberRepository) {
		Member member = Member.createNew("gmoon", "password");
		memberRepository.saveAndFlush(member);
	}

	@Test
	void test() {
		assertThat(repository.findAll()).isNotEmpty();
	}
}

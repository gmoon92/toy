package com.gmoon.demo.member.service;

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.model.MemberOptionUpdate;
import com.gmoon.demo.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberServiceTest {

	final MemberService memberService;
	final MemberRepository memberRepository;
	Long memberId;

	@BeforeEach
	void setUp() {
		Member member = memberRepository.save(Member.newInstance("gmoon"));
		memberId = member.getId();
	}

	@Test
	void updateMemberOption() {
		// given
		MemberOptionUpdate updateMemberOption = new MemberOptionUpdate();
		updateMemberOption.setMemberId(memberId);
		updateMemberOption.setRetired(false);

		// when
		memberService.updateMemberOption(updateMemberOption);
		Member member = memberRepository.findById(memberId)
			.orElseThrow(EntityNotFoundException::new);

		// then
		assertThat(member.getMemberOption().isRetired())
			.isFalse();
	}
}
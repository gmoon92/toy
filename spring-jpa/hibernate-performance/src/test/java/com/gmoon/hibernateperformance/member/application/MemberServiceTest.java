package com.gmoon.hibernateperformance.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import com.gmoon.hibernateperformance.member.domain.Member;
import com.gmoon.hibernateperformance.member.domain.MemberRepository;
import com.gmoon.hibernateperformance.member.model.MemberOptionUpdateRequestVO;

import jakarta.persistence.EntityNotFoundException;
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
		MemberOptionUpdateRequestVO requestVO = new MemberOptionUpdateRequestVO();
		requestVO.setMemberId(memberId);
		requestVO.setRetired(false);

		// when
		memberService.updateMemberOption(requestVO);
		Member member = memberRepository.findById(memberId)
			 .orElseThrow(EntityNotFoundException::new);

		// then
		assertThat(member.getMemberOption().isRetired())
			 .isFalse();
	}
}

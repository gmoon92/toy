package com.gmoon.springframework.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MemberServiceTest {

	@Mock
	MemberRepository memberRepository;

	@Test
	@DisplayName("IoC 개념, memberRepository mocking")
	void save() {
		// given
		Member member = new Member();

		// when
		when(memberRepository.save(member)).thenReturn(member);
		MemberService memberService = new MemberService(memberRepository);

		// then
		assertThat(memberService.save(member))
			.isNotNull()
			.hasFieldOrPropertyWithValue("status", MemberStatus.ENABLE);
	}
}

package com.gmoon.demo.service;

import com.gmoon.demo.domain.Member;
import com.gmoon.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import javax.persistence.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

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
    MemberOptionUpdateVO updateMemberOption = new MemberOptionUpdateVO();
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
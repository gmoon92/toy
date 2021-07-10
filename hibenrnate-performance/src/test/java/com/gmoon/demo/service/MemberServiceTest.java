package com.gmoon.demo.service;

import com.gmoon.demo.domain.Member;
import com.gmoon.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MemberServiceTest {

  @PersistenceContext
  EntityManager entityManager;
  final MemberService memberService;
  final MemberRepository memberRepository;

  @BeforeEach
  void setUp() {
    Member member = Member.newInstance("gmoon");
    memberRepository.save(member);
  }

  @Test
  void updateMemberOption() {
    // given
    MemberOptionUpdate updateMemberOption = new MemberOptionUpdate();
    updateMemberOption.setUsername("gmoon");
    updateMemberOption.setRetired(false);

    // when
    memberService.updateMemberOption(updateMemberOption);

    // then
    Member member = memberRepository.findByName("gmoon");
    assertThat(member.getMemberOption().isRetired())
            .isFalse();
  }
}
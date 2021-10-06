package com.gmoon.springframework.member;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MemberService {

  private final MemberRepository memberRepository;

  public MemberService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public Member save(Member member) {
    member.setCreatedDt(LocalDateTime.now());
    member.setStatus(MemberStatus.ENABLE);
    return memberRepository.save(member);
  }

}

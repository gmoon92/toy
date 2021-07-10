package com.gmoon.demo.service;

import com.gmoon.demo.domain.Member;
import com.gmoon.demo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  @Transactional
  public void updateMemberOption(MemberOptionUpdate memberOptionUpdate) {
    Member member = memberRepository.findByName(memberOptionUpdate.getUsername());

    if (memberOptionUpdate.isRetired()) {
      member.getMemberOption().disabled();
    } else {
      member.getMemberOption().enabled();
    }
  }
}

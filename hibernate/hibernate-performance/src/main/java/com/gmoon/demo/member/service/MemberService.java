package com.gmoon.demo.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.demo.member.domain.Member;
import com.gmoon.demo.member.model.MemberOptionUpdate;
import com.gmoon.demo.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public void updateMemberOption(MemberOptionUpdate memberOptionUpdate) {
		Member member = memberRepository.getOne(memberOptionUpdate.getMemberId());
		member.changeMemberOption(memberOptionUpdate);
	}
}

package com.gmoon.springframework.member;

import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public Member save(Member member) {
		member.setCreatedAt(Instant.now());
		member.setStatus(MemberStatus.ENABLE);
		return memberRepository.save(member);
	}

}
